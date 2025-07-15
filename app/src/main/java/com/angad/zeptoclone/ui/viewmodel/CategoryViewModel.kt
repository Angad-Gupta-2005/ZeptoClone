package com.angad.zeptoclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angad.zeptoclone.data.models.fakeApi.CategoryUiState
import com.angad.zeptoclone.data.repository.CategoryRepository
import com.angad.zeptoclone.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "Category ViewModel"

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState = _uiState.asStateFlow()

    //    Function that load the categories and its product by using id or name
    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            //    Start loading
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    categoryId = categoryId
                )
            }

            try {
                Log.d(TAG, "Loading category: $categoryId ")
                //    Fetching the category by id or name
                val category = categoryRepository.getCategoriesByIdOrName(categoryId)
                Log.d(TAG, "Fetched category ${category != null}")
                if (category != null) {
                    Log.d(TAG, "loadCategory: ${category.name}")

                    try {
                        //    Collect the first emission from the flow to get the List<Product>
                        val products =
                            productRepository.getProductsByCategory(category.name).first()
                        Log.d(TAG, "loadCategory: ${products.size}")

                        if (products.isEmpty()) {
                            Log.d(TAG, "No products found for category: ${category.name}")

                            //    Now try different approach i.e., fetch the products by categoryId
                            val directProducts =
                                productRepository.getProductsByCategory(categoryId).first()
                            if (directProducts.isNotEmpty()) {
                                Log.d(TAG, "Fetched products by categoryId: ${directProducts.size}")
                                _uiState.update {
                                    it.copy(
                                        categoryName = formatCategoryName(category.name),
                                        products = directProducts,
                                        isLoading = false
                                    )
                                }
                                return@launch
                            }

                        //    If we still have no product, try to fetch all products and filter
                            val allProduct = productRepository.getProducts().first()
                            Log.d(TAG, "Total product available ${allProduct.size}")

                        //    Log all unique categories to debug
                            val availableCategory = allProduct.mapNotNull { it.category }.distinct()
                            Log.d(TAG, "Available category in the products are $availableCategory")

                        //    Try case-insensitive matching
                            val filteredProducts = allProduct.filter { product ->
                                product.category?.lowercase() == category.name.lowercase() ||
                                        product.category?.lowercase() == categoryId.lowercase()
                            }

                            Log.d(TAG, "Filtered products: ${filteredProducts.size}")
//90
                            _uiState.update { it.copy(
                                categoryName = formatCategoryName(category.name),
                                products = filteredProducts,
                                isLoading = false
                            ) }
                        } else {
                            _uiState.update{ it.copy(
                                categoryName = formatCategoryName(category.name),
                                products = products,
                                isLoading = false
                            )}
                        }
                    } catch (e: Exception){
                        //    Handle the error
                        Log.d(TAG, "Error loading products: ${e.message}", e)
                        _uiState.update { it.copy(
                            error =  "Error loading products ${e.message}",
                            isLoading = false   //  109
                        ) }
                    }
                } else {
                //    We couldn't find the category, try direct loading by categoryId
                    Log.d(TAG, "Category not found, trying direct loading by categoryId: $categoryId")

                    try {
                        val directProducts = productRepository.getProductsByCategory(categoryId).first()
                        if (directProducts.isNotEmpty()) {
                            Log.d(TAG, "Fetched products by categoryId: ${directProducts.size}")
                            //    Update the ui
                            _uiState.update { it.copy(
                                categoryName = formatCategoryName(categoryId),
                                products = directProducts,
                                isLoading = false
                            ) }
                        } else {    //124
                            val allProducts = productRepository.getProducts().first()
                            val filteredProducts = allProducts.filter { product ->
                                product.category?.lowercase() == categoryId.lowercase()
                            }

                            if(filteredProducts.isNotEmpty()){
                                _uiState.update { it.copy(
                                    categoryName = formatCategoryName(categoryId),
                                    products = filteredProducts,
                                    isLoading = false
                                ) }
                            } else {
                                _uiState.update { it.copy(
                                    error = "No products found for this category: $categoryId",
                                    isLoading = false
                                ) }
                            }
                        }
                    } catch (e: Exception){ //  146
                        //    Handle the error
                        _uiState.update { it.copy(
                            categoryName = formatCategoryName(categoryId),
                            error = "Category not found ${e.message}",
                            isLoading = false
                        ) }
                    }
                }
            } catch (e: Exception){
                //    Handle the error
                _uiState.update { it.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                ) }
            }
        }
    }

//    Function that format the category name as first char capital
    private fun formatCategoryName(categoryName: String): String {
        return when(categoryName.lowercase()){
            "electronics" -> "Electronics"
            "jewelery" -> "Jewelery"
            "men's clothing" -> "Men's Clothing"
            "women's clothing" -> "Women's Clothing"
            else -> categoryName.split(" ")
                .joinToString { it.replaceFirstChar { char -> char.uppercase() } }
        }
    }
}