package com.angad.zeptoclone.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.angad.zeptoclone.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    //    Load the products from the repository
    val products: StateFlow<List<Product>> = productRepository.getProducts()
        .catch { e ->
            _error.value = e.message ?: "Unknown error occurred"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    //    Calling the function that handles the state when the ViewModel is created
    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            try {
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _loading.value = false
            }
        }
    }

    //    Function that fetch the products by its id
    fun getProductById(id: String): Product? {
        val idInt = id.toIntOrNull()
        return idInt?.let { idAsInt ->
            products.value.find { it.id == idAsInt }
        }
    }

    //    Function that refresh the products when needed
    fun refreshProducts() {
        loadProducts()
    }
}