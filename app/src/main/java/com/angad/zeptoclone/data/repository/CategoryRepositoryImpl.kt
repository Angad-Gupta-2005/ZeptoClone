package com.angad.zeptoclone.data.repository

import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.api.FakeStoreApiService
import com.angad.zeptoclone.data.models.fakeApi.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CategoryRepositoryImpl"

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val apiService: FakeStoreApiService
) : CategoryRepository {
    override fun getCategories(): Flow<List<Category>> = flow {
        val categories = apiService.fetchCategories()
        val mappedCategories = mapApiCategoriesToUiCategories(categories)
        emit(mappedCategories)
    }


    override suspend fun getCategoriesByIdOrName(idOrName: String): Category? {
        val categories = mapApiCategoriesToUiCategories(apiService.fetchCategories())
        val id = idOrName.toIntOrNull()
        if (id != null) {
            return categories.find { it.id == id }
        }
    //    If not id, try to match by name
        return categories.find {
            it.name.equals(idOrName, ignoreCase = true) || it.name.replace(" ", "")
                .equals(idOrName.replace(" ", ""), ignoreCase = true)
        }
    }

    private fun mapApiCategoriesToUiCategories(categories: List<String>): List<Category> {
        val allCategory = Category(0, "All", R.drawable.ic_launcher_foreground)
        val mappedCategories = categories.mapIndexed { index, categoryName ->
            val iconRes = when (categoryName.lowercase()) {
                "electronics" -> R.drawable.ic_electronics
                "jewelery" -> R.drawable.jewelry
                "men's clothing" -> R.drawable.mens
                "women's clothing" -> R.drawable.women
                else -> R.drawable.all
            }
            Category(index + 1, formatCategoryName(categoryName), iconRes)
        }

        return listOf(allCategory) + mappedCategories
    }

    //    Formating the first letter of the category name to uppercase
    private fun formatCategoryName(name: String): String {
        return name.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

    }
}