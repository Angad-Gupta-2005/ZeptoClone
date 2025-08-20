package com.angad.zeptoclone.data.repository

import android.content.Context
import android.util.Log
import com.angad.zeptoclone.data.models.payment.PaymentRecord
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val context: Context
) {
    private val TAG = "PaymentRepository"
    private val usersCollection = firestore.collection("users")
    private val ordersCollection = firestore.collection("orders")

    init {
        Log.d(
            TAG,
            "PaymentRepository Initialised: users ${usersCollection.path}, orders: ${ordersCollection.path} "
        )

        //    Ensure we have a user ID for payment
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "PaymentRepository: User not logged in")
        } else {
            Log.d(TAG, "User logged in: ${currentUser.uid.take(5)}... ")
        }
    }

    //    Save payment record to firestore as a subCollection of users
    suspend fun savePayment(
        paymentId: String,
        orderId: String,
        amount: Double,
        itemCount: Int,
        items: List<String>,
        itemImageUrls: Map<String, String>
    ): Result<PaymentRecord> {

        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "savePayment: User not logged in")
                return Result.failure(Exception("User not logged in"))
            }

            val userId = currentUser.uid
            val userEmail = currentUser.email ?: ""
            val userPhone = currentUser.phoneNumber ?: ""

            //    Create payment record object
            val paymentRecord = PaymentRecord(
                id = paymentId,
                orderId = orderId,
                amount = amount,
                timeStamp = Date(),
                userEmail = userEmail,
                userPhone = userPhone,
                status = "SUCCESS",
                userId = userId,
                items = items,
                itemImageUrls = itemImageUrls
            )

            Log.d(
                TAG,
                "Saving payment record to Firestore - document path: users/$userId/payments/$paymentId"
            )

            try {
                //    Check if user document exists & create if needed
                val userDoc = usersCollection.document(userId).get().await()
                if (!userDoc.exists()) {
                    //    Create base user document with minimal info
                    val userData = mapOf(
                        "userId" to userId,
                        "email" to userEmail,
                        "phoneNumber" to userPhone,
                        "createdAt" to Date()
                    )
                    usersCollection.document(userId).set(userData).await()
                }

                //    Save payment to subcollection
                usersCollection.document(userId)
                    .collection("payments")
                    .document(paymentId)
                    .set(paymentRecord)
                    .await()

                //    Update user document with latest payment summary
                usersCollection.document(userId).update(
                    mapOf(
                        "lastPaymentId" to paymentId,
                        "lastPaymentAmount" to amount,
                        "lastPaymentDate" to Date()
                    )
                ).await()

                Log.d(
                    TAG,
                    "savePayment: Payment Successfully saved to users/$userId/payments/$paymentId"
                )
                Result.success(paymentRecord)
            } catch (e: Exception) {
                Log.e(TAG, "savePayment: $e", e)
                e.printStackTrace()
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "savePayment: $e", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    //    Get payment record from firestore using payment id
    suspend fun getPayment(paymentId: String): Result<PaymentRecord> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val userId = currentUser.uid

            val document = usersCollection.document(userId)
                .collection("payments")
                .document(paymentId)
                .get()
                .await()

            if (document.exists()) {
                val payment = document.toObject(PaymentRecord::class.java)

                if (payment != null) {
                    Log.d(
                        TAG,
                        "getPayment: Successfully retrieved payment - amount ${payment.amount}, status: ${payment.status}"
                    )
                    payment.itemImageUrls?.let {
                        Log.d(TAG, "getPayment: Retrieved payment has ${it.size}")
                    }
                    Result.success(payment)
                } else {
                    Result.failure(Exception("Payment record not found"))
                }
            } else {
                Result.failure(Exception("Payment record not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPayment: $e", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    //    Get all payment for current user
    suspend fun getUserPayments(): Result<List<PaymentRecord>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null){
                return Result.failure(Exception("User not logged in"))
            }

            val userId = currentUser.uid

            try {
                val querySnapshot = usersCollection.document(userId)
                    .collection("payments")
                    .orderBy("timestamp")
                    .get()
                    .await()

                val payments = querySnapshot.documents.mapNotNull {
                    try {
                        it.toObject(PaymentRecord::class.java)
                    } catch (e: Exception){
                        null
                    }
                }
                Result.success(payments)
            } catch (e: Exception){
                e.printStackTrace()
                throw e //  rethrow to be catch by outer try-catch
            }
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }

//    Get payment count for current user
    suspend fun getUserPaymentCount(): Result<Int>{
        return try {
            val paymentsResult = getUserPayments()

            if (paymentsResult.isSuccess){
                val count = paymentsResult.getOrNull()?.size?: 0
                Log.d(TAG, "getUserPaymentCount: $count")
                Result.success(count)
            } else{
                val exception = paymentsResult.exceptionOrNull() ?: Exception("Unknown error getting payment count")
                Log.e(TAG, "getUserPaymentCount: $exception", exception)
                Result.failure(exception)
            }
        } catch (e: Exception){
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
