package com.angad.zeptoclone.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.angad.zeptoclone.data.api.FakeStoreApiService
import com.angad.zeptoclone.utils.CartJsonAdapters
import com.angad.zeptoclone.data.models.fakeApi.CartItem
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    providedGson: Gson? = null
) : CartRepository {

    companion object {
        private const val PREF_NAME = "zepto_cart_preferences"
        private const val CART_ITEMS_KEY = "cart_items"
        private const val TAG = "PersistentCartRepository"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson: Gson = CartJsonAdapters.createGson()

    //    Stateflow to hold the current cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    init {
        //    Load saved cart items when repository is initialised
        loadCartItems()
    }

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()

    override fun getTotalItems(): Flow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }

    override fun getTotalPrice(): Flow<Double> = _cartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }

    //    Function that add a product to the cart or increase its quantity if it already exists
    override suspend fun addToCard(product: Product) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }

            val updatedCart = if (existingItem != null) {
                //    If the item already exists, update its quantity
                currentCart.map { cartItem ->
                    if (cartItem.product.id == product.id) {
                        cartItem.copy(quantity = cartItem.quantity + 1)
                    } else {
                        cartItem
                    }
                }
            } else {
                //    If the item doesn't exist, add it to the cart
                currentCart + CartItem(product = product, quantity = 1)
            }
            //    Persist the updated cart items
            saveCartItems(updatedCart)
            updatedCart
        }
    }

    override suspend fun removeFromCart(product: Product) {
        _cartItems.update { currentCart ->
            val existingItem = currentCart.find { it.product.id == product.id }

            val updatedCart = if (existingItem != null && existingItem.quantity > 1) {
                //    Decrement the quantity if the item exists and is more than 1
                currentCart.map { cartItem ->
                    if (cartItem.product.id == product.id) {
                        cartItem.copy(quantity = cartItem.quantity - 1)
                    } else {
                        cartItem
                    }
                }
            } else {
                currentCart.filter { it.product.id != product.id }
            }
            //    Persist the updated cart items
            saveCartItems(updatedCart)
            updatedCart
        }
    }

    override suspend fun setQuantity(product: Product, quantity: Int) {
        _cartItems.update { currentCart ->
            val updatedCart = if (quantity <= 0) {
                //    Remove from cart if quantity is 0 or less
                currentCart.filter { it.product.id != product.id }
            } else {
                val existingItem = currentCart.find { it.product.id == product.id }

                if (existingItem != null) {
                    currentCart.map { cartItem ->
                        if (cartItem.product.id == product.id) {
                            cartItem.copy(quantity = quantity)
                        } else {
                            cartItem
                        }
                    }
                } else {
                    //    Add to cart if it doesn't exist
                    currentCart + CartItem(product = product, quantity = quantity)
                }
            }
            //    Persist the updated cart items
            saveCartItems(updatedCart)
            updatedCart
        }
    }


    override suspend fun getQuantity(productId: Int): Int = _cartItems.value.find {
        it.product.id == productId
    }?.quantity ?: 0


    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    private fun saveCartItems(items: List<CartItem>) {
        try {
            val json = gson.toJson(items)
            sharedPreferences.edit().putString(CART_ITEMS_KEY, json).apply()
            Log.d(TAG, "Cart saved to SharedPreferences, items: ${items.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cart items: ${e.message}", e)
            _cartItems.value = emptyList()
        }
    }

    private fun loadCartItems() {
        try {
            val json = sharedPreferences.getString(CART_ITEMS_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val loadedItems: List<CartItem> = gson.fromJson(json, type)
                _cartItems.value = loadedItems
                Log.d(TAG, "Cart loaded from SharedPreferences, item: ${loadedItems.size}")
            } else {
                _cartItems.value = emptyList()
                Log.d(TAG, "No cart items found in SharedPreferences")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cart items: ${e.message}", e)
            _cartItems.value = emptyList()
        }
    }
}