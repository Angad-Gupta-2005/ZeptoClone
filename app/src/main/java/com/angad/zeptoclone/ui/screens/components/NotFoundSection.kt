package com.angad.zeptoclone.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun NotFoundSection(
    imageLoader: ImageLoader,
    sadEmojiRequest: ImageRequest,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        //    First part of text - exactly matching reference
        Text(
            text = "didn't find",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Normal,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        )

        //    Second part of text - exactly matching reference
        Text(
            text = "what you were",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Normal,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        )

        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //    Third part of text - exactly matching reference
            Text(
                text = "looking for?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            //    Sad emoji image
            AsyncImage(
                model = sadEmojiRequest,
                contentDescription = "Sad emoji",
                imageLoader = imageLoader,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(37.dp)
            )
        }

        //    Final text
        Text(
            text = "Suggest something & we'll look into it",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Normal,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        )
    }

}

