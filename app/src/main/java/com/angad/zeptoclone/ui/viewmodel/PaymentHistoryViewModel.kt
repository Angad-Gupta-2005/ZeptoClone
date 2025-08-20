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
class PaymentHistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentHistoryState>(PaymentHistoryState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadPaymentHistory()
    }

    fun loadPaymentHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PaymentHistoryState.Loading

            try {
                val result = paymentRepository.getUserPayments()
                result.fold(
                    onSuccess = { payments ->
                        if (payments.isEmpty()) {
                            _uiState.value = PaymentHistoryState.Empty
                        } else {
                            _uiState.value = PaymentHistoryState.Success(payments)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = PaymentHistoryState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = PaymentHistoryState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class PaymentHistoryState {
    data object Loading : PaymentHistoryState()
    data object Empty : PaymentHistoryState()
    data class Success(val payments: List<PaymentRecord>) : PaymentHistoryState()
    data class Error(val message: String) : PaymentHistoryState()
}