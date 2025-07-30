package com.angad.zeptoclone.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angad.zeptoclone.data.models.fakeApi.CartItem
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.angad.zeptoclone.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
//    private val paymentRepository: PaymentRepository
) : ViewModel() {

    //    State flow to hold the current cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    //    State flow that count total items in cart
    private val _totalItems = MutableStateFlow(0)
    val totalItems = _totalItems.asStateFlow()

    //    State flow that calculate total price of items in cart
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice = _totalPrice.asStateFlow()

    //    State flow for tip amount selected by user
    private val _tipAmount = MutableStateFlow(0)
    val tipAmount = _tipAmount.asStateFlow()

    //    State flow for total amount to pay (items price + tip)
    private val _totalWithTip = MutableStateFlow(0.0)
    val totalWithTip = _totalWithTip.asStateFlow()

    //    State flow for free delivery states
    private val _isFreeDeliveryApplied = MutableStateFlow(false)
    val isFreeDeliveryApplied = _isFreeDeliveryApplied.asStateFlow()

    private val _isApplyingFreeDelivery = MutableStateFlow(false)
    val isApplyingFreeDelivery = _isApplyingFreeDelivery.asStateFlow()

    //    bottom sheet visibility
    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible = _isBottomSheetVisible.asStateFlow()

    //    Final total amount including all charges
    private val _finalTotal = MutableStateFlow(0.0)
    val findTotal = _finalTotal.asStateFlow()

    //    Constant
    private val MIN_CART_VALUE_FOR_FREE_DELEVERY = 200.0
    private val DELEVERY_FEE = 30.0

    //    Initialize by collecting data from repository
    init {
        viewModelScope.launch(Dispatchers.IO) {
            //    Collect cart items from repository
            cartRepository.getCartItems().collect { items ->
                _cartItems.value = items
                updateCartTotals()
            }
        }
    }

    fun getTotalItems(): Int {
        return _totalItems.value
    }

    //    Function that add a product to the cart or increase its quantity if it already exists
    fun addToCart(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.addToCard(product)
        }
    }

    //    Function that remove a product from the cart or decrease its quantity if it already exists
    fun removeFromCart(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.removeFromCart(product)
        }
    }

    //    Function update quantity for a product
    fun updateQuantity(product: Product, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.setQuantity(product, quantity)
        }
    }

    //    Function that fetch the quantity of a specific product in the cart
    fun getQuantity(productId: Product): Int {
        return _cartItems.value.find { it.product.id == productId.id }?.quantity ?: 0
    }

    //    Check if product in the cart
    fun isInCart(product: Product): Boolean {
        return _cartItems.value.any { it.product.id == product.id }
    }

    //    Clear the entire cart
    fun clearCart() {
        viewModelScope.launch(Dispatchers.IO) {
            cartRepository.clearCart()
        }
    }

    //    Set a tip amount selected by user
    fun setTipAmount(amount: Int) {
        _tipAmount.value = amount
        updateTotalWithTip()
        updateFinalTotal()
    }

    //    Get the current selected tip amount
    fun getTipAmount(): Int {
        return _tipAmount.value
    }

    //    Show the bill summary using bottom sheet
    fun showBottomSheet() {
        _isBottomSheetVisible.value = true
    }

    //    Hide the bottom sheet
    fun hideBottomSheet() {
        _isBottomSheetVisible.value = false
    }

    private fun updateTotalWithTip() {
        _totalWithTip.value = _totalPrice.value + _tipAmount.value
    }

    private fun updateFinalTotal() {
        //    Calculate discount price (10% OFF)
        val discountPrice = (_totalPrice.value * 0.9).roundToInt().toDouble()

        //    Add delivery fee if applicable
        val withDelivery = if (_isFreeDeliveryApplied.value) {
            discountPrice
        } else {
            discountPrice + DELEVERY_FEE
        }

        //    Add tip
        _finalTotal.value = withDelivery + _tipAmount.value
    }

    private fun calculateTotalPrice() {
        val itemCost = _totalPrice.value
        val handlingCost = 14.99
        val gstOnHandling = 2.48

        //    Determine if it is late night
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val isLateNight = currentHour >= 23 || currentHour < 6
        val lateNightFee = if (isLateNight) 25.0 else 0.0
        val gstOnLateNight = if (isLateNight) 4.13 else 0.0

        //    Delivery fee
        val deliveryFee = if (_isFreeDeliveryApplied.value) 0.0 else 30.0

        //    Calculate total with all components
        val exactItemTotal =
            itemCost + handlingCost + gstOnHandling + (if (isLateNight) gstOnLateNight else 0.0)
        val totalWithAllFees = exactItemTotal + deliveryFee + _tipAmount.value + lateNightFee

        //    Update the finalTotal with the consistent calculation
        _finalTotal.value = totalWithAllFees

        //    Update totalWithTip for the bottom sheet
        _totalWithTip.value = totalWithAllFees
    }

    fun applyFreeDelivery() {
        _isApplyingFreeDelivery.value = true
        //    Simulate network delay
        viewModelScope.launch {
            delay(800)
            _isFreeDeliveryApplied.value = true
            _isApplyingFreeDelivery.value = false
            calculateTotalPrice()   //    recalculate with free delivery
        }
    }


    private fun updateCartTotals() {
        var total = 0.0
        var count = 0

        for (item in _cartItems.value) {
            total += item.product.price * item.quantity
            count += item.quantity
        }

        _totalPrice.value = total
        _totalItems.value = count

        //    Recalculate the final price with all fees
        calculateTotalPrice()
    }

    //    Payment section
    fun generateOrderId(): String {
        return "Order_" + System.currentTimeMillis()
    }

}