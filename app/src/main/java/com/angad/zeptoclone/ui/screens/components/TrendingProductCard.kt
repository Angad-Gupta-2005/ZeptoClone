package com.angad.zeptoclone.ui.screens.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.models.fakeApi.Product
import java.util.Locale

private const val TAG = "TrendingProductCard"

@Composable
fun TrendingProductCard(
    product: Product,
    onCategoryClick: (String) -> Unit
) {

    LaunchedEffect(product.id) {
        Log.d(
            TAG,
            "Rendering TrendingProductCard for product: ${product.id}, name: ${product.name}"
        )
    }

    var isFavorite by remember { mutableStateOf(false) }

    val discountPercent = when(product.category?.lowercase()){
        "electronics" -> "UP TO 80% OFF"
        "jewelery" -> "UP TO 60% OFF"
        "men's clothing" -> "UP TO 70% OFF"
        "women's clothing" -> "UP TO 85% OFF"
        else -> "UP TO 50% OFF"
    }

//    Wrap card and text in a column
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable { product.category?.let { category -> onCategoryClick(category) } },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
        //    Discount banner with glass effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .blur(radius = 0.5.dp)
            ) {
                Text(
                    text = discountPercent,
                    color = Color(0xFF2B0466),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier.background(Color.White)
            ){
            //    Image fill the entire card
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = product.imageUrl).apply(block = fun ImageRequest.Builder.() {
                                placeholder(product.imageRes)
                                error(R.drawable.all)
                                crossfade(true)
                            }).build()
                    ),
                    contentDescription = product.name ?: "Product Image",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    alignment = Alignment.BottomCenter
                )
            }
        }

        Text(
            text = formatCategoryName(product.category?:"General"),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 4.dp)
        )
    }
}

private fun formatCategoryName(category: String): String{
    return when(category.lowercase()){
        "electronics" -> "Electronics"
        "jewelery" -> "Jewelery"
        "men's clothing" -> "Men's Clothing"
        "women's clothing" -> "Women's Clothing"
        else -> category.split(" ").joinToString(" ") { it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        } }
    }
}