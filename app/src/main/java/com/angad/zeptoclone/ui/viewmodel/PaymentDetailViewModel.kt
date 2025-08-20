package com.angad.zeptoclone.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angad.zeptoclone.data.models.payment.PaymentRecord
import com.angad.zeptoclone.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentDetailViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PaymentDetailState>(PaymentDetailState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadPaymentDetail(paymentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PaymentDetailState.Loading

            try {
                val result = paymentRepository.getPayment(paymentId)
                result.fold(
                    onSuccess = { payment ->
                        _uiState.value = PaymentDetailState.Success(payment)
                    },
                    onFailure = { error ->
                        _uiState.value = PaymentDetailState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = PaymentDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class PaymentDetailState {
    data object Loading : PaymentDetailState()
    data class Success(val payment: PaymentRecord) : PaymentDetailState()
    data class Error(val message: String) : PaymentDetailState()
}