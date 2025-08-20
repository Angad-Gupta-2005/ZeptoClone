package com.angad.zeptoclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.angad.zeptoclone.data.models.payment.PaymentRecord
import com.angad.zeptoclone.data.repository.PaymentRepository
import com.angad.zeptoclone.utils.PaymentStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    private val TAG = "PaymentViewModel"

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance().also {
            Log.d(TAG, "FirebaseFirestore instance is initialized successfully")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    //    Payment information
    private val _amount = MutableStateFlow(0.0)
    val amount = _amount.asStateFlow()

    private val _orderId = MutableStateFlow("")
    val orderId = _orderId.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userPhone = MutableStateFlow("")
    val userPhone = _userPhone.asStateFlow()

    private val _itemImageUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val itemImageUrls = _itemImageUrls.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _paymentStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val paymentStatus = _paymentStatus.asStateFlow()

    private var navController: NavController? = null

    //    Set the navController for navigation
    fun setNavController(controller: NavController) {
        navController = controller
    }

    //    Gets the navController if available
    fun getNavController(): NavController? {
        return navController
    }

    //    Set the user's email address
    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    //    Set the user's phone number
    fun setUserPhone(phone: String) {
        _userPhone.value = phone
    }

    //    Set the payment amount
    fun setAmount(amount: Double) {
        _amount.value = amount
    }

    //    Set the order ID
    fun setOrderId(orderId: String) {
        _orderId.value = orderId
    }

    //    Set the item image Urls
    fun setItemImageUrls(itemImageUrls: Map<String, String>) {
        _itemImageUrls.value = itemImageUrls
    }

    //    Adds a single item image urls
    fun addItemImageUrls(itemId: String, imageUrl: String) {
        val currentMap = _itemImageUrls.value.toMutableMap()
        currentMap[itemId] = imageUrl
        _itemImageUrls.value = currentMap
    }

    //    Method to update the payment status from outside
    fun updatePaymentStatus(status: PaymentStatus) {
        _paymentStatus.value = status
    }

    fun setPaymentInfo(amount: Double, orderId: String, userEmail: String, userPhone: String) {
        _amount.value = amount
        _orderId.value = orderId
        _userEmail.value = userEmail
        _userPhone.value = userPhone
        Log.d(
            TAG,
            "setPaymentInfo: amount: $amount, orderId: $orderId, userEmail: $userEmail, userPhone: $userPhone"
        )
    }

    fun savePaymentViaRepository(
        paymentId: String,
        orderId: String,
        amount: Double,
        items: List<String> = emptyList(),
        itemImageUrls: Map<String, String> = _itemImageUrls.value
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = paymentRepository.savePayment(
                    paymentId = paymentId,
                    orderId = orderId,
                    amount = amount,
                    itemCount = items.size,
                    items = items,
                    itemImageUrls = itemImageUrls
                )

                if (result.isSuccess) {
                    Log.d(TAG, "savePaymentViaRepository: successful")
                } else {
                    val exception = result.exceptionOrNull()
                    exception?.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun savePaymentRecord(
        paymentId: String,
        orderId: String,
        amount: Double,
        items: List<String>,
        itemImageUrls: Map<String, String> = _itemImageUrls.value
    ): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = paymentRepository.savePayment(
                    paymentId = paymentId,
                    orderId = orderId,
                    amount = amount,
                    itemCount = items.size,
                    items = items,
                    itemImageUrls = itemImageUrls
                )

                result.fold(
                    onSuccess = {
                        Log.d(TAG, "savePaymentRecord: Payment Record saved successfully")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "savePaymentRecord: Failed to save payment recode ${e.message}", e)
                    }
                )
            } catch (e: Exception){
                Log.e(TAG, "savePaymentRecord: Exception while saving payment record", e)
            }
        }
    }

    fun getPaymentDetails(
        paymentId: String,
        onSuccess: (PaymentRecord) -> Unit,
        onError: (String) -> Unit
    ){
        Log.i(TAG, "getPaymentDetails: Starting payment with paymentId $paymentId")
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
                val repoResult = paymentRepository.getPayment(paymentId)

                if (repoResult.isSuccess){
                    val payment = repoResult.getOrThrow()
                    payment.itemImageUrls?.let {
                        if (it.isNotEmpty()){
                            Log.d(TAG, "getPaymentDetails: Retrieved payment has ${it.size} image urls")
                        }
                    }
                    _isLoading.value = false
                    onSuccess(payment)
                    return@launch
                } else {
                    val exception = repoResult.exceptionOrNull()
                    Log.w(TAG, "getPaymentDetails: Failed to get payment from repository")
                //    Continue to fallback to Firestore
                }

                if (firestore == null){
                    _isLoading.value = false
                    onError("Firestore is not available")
                    return@launch
                }

                try {
                    val document = firestore.collection("payments").document(paymentId).get().await()

                    if (document.exists()){
                        val payment = document.toObject(PaymentRecord::class.java)

                        if (payment != null){
                            onSuccess(payment)
                        } else {
                            createAndRunDummyPayment(paymentId, "SUCCESS (Local - Conversion Failed)", onSuccess)
                        }
                    } else{
                        createAndRunDummyPayment(paymentId, "SUCCESS (Local - Not Found)", onSuccess)
                    }
                } catch (e: FirebaseFirestoreException){
                    e.printStackTrace()
                    createAndRunDummyPayment(paymentId, "SUCCESS (Local - Firestore Error)", onSuccess)
                }
                _isLoading.value = false
            } catch (e: Exception){
                e.printStackTrace()
                _isLoading.value = false

                val errorMessage = e.message ?: "Unknown error occurred"
                onError(errorMessage)
            }
        }
    }

    private fun createAndRunDummyPayment(
        paymentId: String,
        status: String,
        onSuccess: (PaymentRecord) -> Unit
    ){
        val dummyPayment = PaymentRecord(
            id = paymentId,
            orderId = _orderId.value,
            amount = _amount.value,
            timeStamp = Date(),
            userEmail = _userEmail.value,
            userPhone = _userPhone.value,
            status = status,
            itemImageUrls = _itemImageUrls.value
        )
        onSuccess(dummyPayment)
    }

//    Helper function to handle firestore error consistently
    private fun handleFirestoreError(
        errorMessage: String,
        continueSuccess: (() -> Unit)?,
        reportError: ((String) -> Unit)?
    ){
        _isLoading.value = false
        if (reportError == null && continueSuccess != null){
            continueSuccess()
        } else if (reportError != null){
            reportError(errorMessage)
        } else {
            Log.w(TAG, "handleFirestoreError: Both continueSuccess and reportError are null - no action are taken")
        }
    }

    override fun onCleared() {
        Log.i(TAG, "onCleared: PaymentViewModel is cleared")
        super.onCleared()
    }
}