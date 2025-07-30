package com.angad.zeptoclone.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.models.fakeApi.Category
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.angad.zeptoclone.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    //    Cache of products by category to avoid repeated networks calls
    private val productCache = mutableMapOf<String, List<Product>>()

    init {
        fetchData()
    }

    private fun fetchData() {
        fetchCategories()
        fetchAllProducts()
    }

    private fun fetchCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                productRepository.getCategories()
                    .flowOn(Dispatchers.IO)
                    .collect { apiCategories ->
                        val mappedCategories = withContext(Dispatchers.Default) {
                            mapApiCategoriesToUiCategories(apiCategories)
                        }
                        _categories.value = mappedCategories
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load category ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    //    Function that fetch all products
    private fun fetchAllProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                //    Check cache first
                if (productCache.containsKey("all")) {
                    _products.value = productCache["all"] ?: emptyList()
                    _isLoading.value = false
                    return@launch
                }

                productRepository.getProducts()
                    .flowOn(Dispatchers.IO)
                    .collect { apiProducts ->
                        val processedProducts = withContext(Dispatchers.Default) {
                            //  Process products on CPU threads
                            processProducts(apiProducts)
                        }
                        _products.value = processedProducts
                        //    Cache the products
                        productCache["all"] = processedProducts
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load products ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    //    Function that fetch the product by category name
    private fun fetchProductsByCategory(categoryName: String) {
        val cacheKey = categoryName.lowercase()
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                //  Check product cache first
                if (productCache.containsKey(cacheKey)) {
                    _products.value = productCache[cacheKey] ?: emptyList()
                    _isLoading.value = false
                    return@launch
                }

                productRepository.getProductsByCategory(categoryName)
                    .flowOn(Dispatchers.IO)
                    .collect { apiProducts ->
                        val processedProducts = withContext(Dispatchers.Default) {
                            processProducts(apiProducts)
                        }
                        _products.value = processedProducts
                        productCache[cacheKey] = processedProducts
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load products ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

//    Function that fetch the selected product category
fun selectCategory(category: Category) {
        if (_selectedCategory.value?.id == category.id){
            //    Don't refresh if already selected
            return
        }
        _selectedCategory.value = category
        if (category.name == "All"){
            fetchAllProducts()
        } else {
            fetchProductsByCategory(category.name.lowercase())
        }
    }

    private fun mapApiCategoriesToUiCategories(apiCategories: List<String>): List<Category> {
        val allCategories = Category(0, "All", R.drawable.all)

        val categoryMapIcon = mapOf(
            "electronics" to R.drawable.ic_electronics,
            "jewelery" to R.drawable.jewelry,
            "men's clothing" to R.drawable.mens,
            "women's clothing" to R.drawable.women
        )

        val mappedCategories = apiCategories.mapIndexed { index, categoryName ->
            val iconRes = categoryMapIcon[categoryName.lowercase()] ?: R.drawable.all
            Category(index + 1, formatCategoryName(categoryName), iconRes)
        }
        return listOf(allCategories) + mappedCategories
    }

    private fun formatCategoryName(name: String): String {
        return name.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }

    //    Function that process the products i.e., fix image URLs and any others processing
    private fun processProducts(products: List<Product>): List<Product> {
        return products.map { product ->
            val fixedImageUrl = when {
                product.imageUrl.isEmpty() -> ""
                !product.imageUrl.startsWith("http") && product.imageUrl.startsWith("/") ->
                    "https://fakestoreapi.com${product.imageUrl}"

                !product.imageUrl.startsWith("http") -> "https://${product.imageUrl}"
                else -> product.imageUrl
            }
            product.copy(imageUrl = fixedImageUrl)
        }
    }

    fun navigateToCategory(categoryId: String){
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.getProductsByCategory(categoryId)
        }
    }

//    This function is now integrated into the processProducts
//    No need for separate fixProductsImageUrls call
    fun fixedProductsImageUrls() {
        viewModelScope.launch(Dispatchers.Default) {
            val existingProducts = _products.value
            val fixedProducts = processProducts(existingProducts)
            _products.value = fixedProducts
        }
    }
}