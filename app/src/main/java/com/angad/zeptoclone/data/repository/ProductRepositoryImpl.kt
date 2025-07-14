package com.angad.zeptoclone.data.repository

import android.util.Log
import com.angad.zeptoclone.data.api.FakeStoreApiService
import com.angad.zeptoclone.data.models.fakeApi.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ProductRepositoryImpl"

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: FakeStoreApiService
) : ProductRepository {

    //    Function that fetch all products
    override fun getProducts(): Flow<List<Product>> = flow {
        Log.d(TAG, "Get all products")
        val products = apiService.fetchProducts()
        Log.d(TAG, "Fetched: ${products.size} products")
        emit(products)
    }.catch { e ->
        Log.e(TAG, "Error fetching products: ${e.message}", e)
        emit(emptyList())
    }

    //    Function that fetch all product by category
    override fun getProductsByCategory(category: String): Flow<List<Product>> = flow {
        Log.d(TAG, "Get all products by category $category")
        val products = apiService.fetchProductsByCategory(category)
        Log.d(TAG, "Fetched: ${products.size} products")
        emit(products)
    }.catch { e ->
        Log.e(TAG, "Error fetching products by category ${e.message} ", e)

    //    Fallback to fetch all products if category fetch fails
    //    Or if category not found in the API than fetch related products by name
        val fallbackProducts = try {
            Log.d(TAG, " Attempting fallback with all products ")
            val allProducts = apiService.fetchProducts()
            Log.d(TAG, "Fetched: ${allProducts.size} products")

        //    Try case-insensitive matching of category names
            val filteredProducts = allProducts.filter {
                it.category?.lowercase() == category.lowercase()
            }
            Log.d(TAG, "Fallback filtered to: ${filteredProducts.size}")
            filteredProducts
        } catch (e2: Exception){
            Log.e(TAG, "Error fetching fallback products: ${e2.message}", e2)
            emptyList()
        }
        emit(fallbackProducts)
    }

    //    Function that fetch product category
    override fun getCategories(): Flow<List<String>> = flow {
        Log.d(TAG, "Getting all categories")
        val categories = apiService.fetchCategories()
        Log.d(TAG, "Total categories: ${categories.size}")
        emit(categories)
    }.catch { e ->
        Log.e(TAG, "Error fetching categories: ${e.message}", e)
        emit(emptyList())
    }
}