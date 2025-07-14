package com.angad.zeptoclone.data.repository

import com.angad.zeptoclone.data.models.fakeApi.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    //    Function that fetch all categories
    fun getCategories(): Flow<List<Category>>

    //    Function that fetch product categories by its name or id
    suspend fun getCategoriesByIdOrName(idOrName: String): Category?

}