package com.angad.zeptoclone.data.models.payment

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class PaymentRecord(
    @DocumentId
    val id: String = "", //  Razorpay payment id
    val orderId: String = "",
    val amount: Double = 0.0,
    val timeStamp: Date = Date(),
    val userEmail: String = "",
    val userPhone: String = "",
    val status: String = "",    //  Success, Failed etc.
    val userId: String = "",    //  Firebase userId
    val deliveryAddress: String = "",
    val items: List<String> = emptyList(),
    val itemCount: Int = 0,
    val paymentMethod: String = "Razorpay",
    val metadata: Map<String, Any> = emptyMap(),
    val itemImageUrls: Map<String, String> = emptyMap()
)
