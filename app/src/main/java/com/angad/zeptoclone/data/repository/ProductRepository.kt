package com.angad.zeptoclone.data.repository

import com.angad.zeptoclone.data.models.fakeApi.Category
import com.angad.zeptoclone.data.models.fakeApi.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    fun getProductsByCategory(category: String): Flow<List<Product>>
    fun getCategories(): Flow<List<String>>
}