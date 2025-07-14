package com.angad.zeptoclone.data.api

import com.angad.zeptoclone.data.models.mealDB.CategoryResponse
import com.angad.zeptoclone.data.models.mealDB.MealResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    @GET("search.php")
    suspend fun searchMeals(
        @Query("s") searchQuery: String
    ): MealResponse

//    @GET("api/json/v1/1/lookup.php")
//    suspend fun getMealById(@Query("i") id: String): MealResponse

    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") categoryName: String): MealResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse
}
