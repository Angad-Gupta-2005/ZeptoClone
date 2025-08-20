package com.angad.zeptoclone.utils

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Processing : PaymentStatus()
    object Success : PaymentStatus()
    object SuccessButNotSaved: PaymentStatus()
    object FirestoreNotEnabled: PaymentStatus()
    data class Error(val message: String) : PaymentStatus()
}