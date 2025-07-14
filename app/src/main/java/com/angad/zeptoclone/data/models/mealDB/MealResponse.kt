package com.angad.zeptoclone.data.models.mealDB

import com.angad.zeptoclone.data.models.fakeApi.Product
import com.angad.zeptoclone.data.models.fakeApi.Rating
import com.google.gson.annotations.SerializedName
import kotlin.random.Random

data class MealResponse(
    @SerializedName("meals")
    val meals: List<MealDto>?
)

data class CategoryResponse(
    @SerializedName("categories")
    val categories: List<CategoryDto>?
)

data class MealDto(
    @SerializedName("idMeal")
    val id: String,

    @SerializedName("strMeal")
    val name: String,

    @SerializedName("strCategory")
    val category: String?,

    @SerializedName("strArea")
    val area: String?,

    @SerializedName("strInstructions")
    val instructions: String?,

    @SerializedName("strMealThumb")
    val thumbnailUrl: String?,

    @SerializedName("strTags")
    val tags: String?
){
//    Convert MealDto to Product model
    fun toProduct(): Product {
        val randomPrice = Random.nextDouble(50.0, 500.0)

        val randomRating = Random.nextDouble(3.5,5.0)

        val randomRatingCount = Random.nextInt(10,200)

        return Product(
            id  = id.toInt(),
            name = name,
            price = randomPrice,
            category = category ?: "",
            imageUrl = thumbnailUrl ?: "",
            imageRes = 0,
            description = instructions ?: "",
            rating = Rating(
                rate = randomRating,
                count = randomRatingCount
            )
        )

    }
}

data class CategoryDto(
    @SerializedName("idCategory")
    val id: String,

    @SerializedName("strCategory")
    val name: String,

    @SerializedName("strCategoryThumb")
    val thumbnailUrl: String,

    @SerializedName("strCategoryDescription")
    val description: String?
){
    fun toCategory():  MealCategory {
        return MealCategory(
            id = id,
            name = name,
            imageRes = thumbnailUrl
        )
    }
}
