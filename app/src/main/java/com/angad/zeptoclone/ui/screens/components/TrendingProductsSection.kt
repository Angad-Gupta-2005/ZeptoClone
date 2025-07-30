package com.angad.zeptoclone.ui.screens.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.angad.zeptoclone.ui.screens.getCategoryGradient
import com.angad.zeptoclone.ui.viewmodel.HomeViewModel

private const val TAG = "TrendingProductsSection"

@SuppressLint("RememberReturnType")
@Composable
fun TrendingProductsSection(
    products: List<Product>,
    viewModel: HomeViewModel,
    onNavigateToCategory: (String) -> Unit,
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(products) {
        Log.d(TAG, "TrendingProductsSection products receive: ${products.size} ")
        products.forEachIndexed { index, product ->
            Log.d(
                TAG,
                "Product $index: id=${product.id}, name=${product.name}, category=${product.category}, imageUrl=${product.imageUrl}, imageRes=${product.imageRes} "
            )
        }
    }

//    Fix image URLs if needed before displaying
    LaunchedEffect(Unit) {
        Log.d(TAG, "Calling fixedProductsImageUrls from TrendingProductsSection")
        viewModel.fixedProductsImageUrls()
    }

    val categoryBackground = selectedCategory?.let {
        getCategoryGradient(it)
    } ?: Brush.horizontalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))

//    Ensure we have 6 products with appropriate category
    val displayProducts = remember(products) {
        val categories = listOf("electronics, jewelery", "men's clothing", "women's clothing")

        if (products.isEmpty()){
            List(6){ index ->
                val categoryIndex = index % categories.size
                val imageRes = when (categoryIndex) {
                    0 -> R.drawable.ic_electronics
                    1 -> R.drawable.jewelry
                    2 -> R.drawable.mens
                    3 -> R.drawable.women
                    else -> R.drawable.all
                }

                Product(
                    id = index,
                    name = "Product $index",
                    price = 100.0,
                    category = categories[categoryIndex],
                    imageUrl = "",
                    imageRes = imageRes,
                )
            }
        } else if (products.size < 6){
        //    Reuse existing products if available
            val result = mutableListOf<Product>()
            repeat(6){ index ->
                val product = products[index % products.size]
            //    Assign different categories to ensure variety
                val category = categories[index % categories.size]
                val modifiedProduct = product.copy(category = category)
                result.add(modifiedProduct)
            }
            result
        } else {
            val groupedByCategory = products.groupBy { it.category?.lowercase()?: "other" }
            val result = mutableListOf<Product>()

        //    First add one for each available category
            categories.forEach { category ->
                groupedByCategory[category]?.firstOrNull()?.let {
                    result.add(it)
                    Log.d(TAG, "Added product: ${it.id} from category $category ")
                }
            }

        //    If we still need more, add remaining products
            if (result.size < 6){
                val remaining = products.filter { it !in result }
                val additionalProduct = remaining.take(6 - result.size)
                result.addAll(additionalProduct)
                Log.d(TAG, "Added products: ${additionalProduct.size}")
            }

            if (result.size < 6){
                val moreProducts = mutableListOf<Product>()
                repeat(6-result.size){ index ->
                    val product = result[index % result.size]
                    moreProducts.add(product)
                }
                result.addAll(moreProducts)
            }

            val finalProducts = result.take(6)
            Log.d(TAG, "Final product for display: ${finalProducts.map { it.id }} ")
            finalProducts
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = categoryBackground,
                shape = RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                    topStart = 0.dp,
                    topEnd = 0.dp
                )
            ).clip(
                RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                    topStart = 0.dp,
                    topEnd = 0.dp
                )
            )
    ) {
        GiftImage()

        Spacer(modifier = Modifier.height(8.dp))

    //    First row 3 items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayProducts.take(3).forEach { product ->
                Box(
                    modifier = Modifier
                        .weight(1f)
//                        .size(160.dp)
                ){
                    TrendingProductCard(product = product, onCategoryClick = onNavigateToCategory)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //    Second row 3 items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayProducts.take(3).forEach { product ->
                Box(
                    modifier = Modifier
                        .weight(1f)
//                        .size(160.dp)
                ){
                    TrendingProductCard(product = product, onCategoryClick = onNavigateToCategory)
                }
            }
        }

        Spacer( modifier = Modifier.height(8.dp))

    //    Promo code banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "Use FLAT20 - Get 20% off on purchase of Rs 3000",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }  
}