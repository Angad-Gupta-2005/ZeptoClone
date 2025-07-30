package com.angad.zeptoclone.data.repository

import com.angad.zeptoclone.data.models.fakeApi.CartItem
import com.angad.zeptoclone.data.models.fakeApi.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    //    Get all items in the cart
    fun getCartItems(): Flow<List<CartItem>>

    //Get total number of items in the cart
    fun getTotalItems(): Flow<Int>

    //  Get total price of the items in the cart
    fun getTotalPrice(): Flow<Double>

    //    Add a product to the cart
    suspend fun addToCard(product: Product)

    //    Remove a product from the cart
    suspend fun removeFromCart(product: Product)

    //    Set a specific quantity for a product
    suspend fun setQuantity(product: Product, quantity: Int)

    //    Get a quantity of a specific product in the cart
    suspend fun getQuantity(productId: Int): Int

    //    Clear the entire cart
    suspend fun clearCart()
}