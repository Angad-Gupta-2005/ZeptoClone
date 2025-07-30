package com.angad.zeptoclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.angad.zeptoclone.data.models.fakeApi.Category
import com.angad.zeptoclone.ui.screens.components.CategoryItem

@Composable
fun CategoriesSection(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    selectedCategory: Category?
) {
    val lazyListState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            LazyRow(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategorySelected(category) },
                        isSelected = category.id == selectedCategory?.id
                    )
                }
            }

        //    White tab indicator line at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White)
            )
        }
    }
}
