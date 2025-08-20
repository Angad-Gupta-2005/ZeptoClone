package com.angad.zeptoclone.data.models.payment


//    Data class to hold parsed item details
data class ItemDetails(val name: String, val priceQuantity: String)

data class PriceQuantityInfo(
    val price: String,
    val quantity: String,
    val subTotal: String
)