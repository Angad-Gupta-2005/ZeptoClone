package com.angad.zeptoclone.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.angad.zeptoclone.ui.viewmodel.LocationViewModel
import com.angad.zeptoclone.utils.LocationPermissionHandler

@Composable
fun LocationBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = Color.Black,
    onLocationClick: () -> Unit = {}
) {
//    Wrap with permission handler
    LocationPermissionHandler {
        //    This content will only be shown if the location permission is granted
        LocationBarWithLiveAddress(
            modifier = modifier,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            onLocationClick = onLocationClick
        )
    }
}

@Composable
fun LocationBarWithLiveAddress(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = Color.White,
    viewModel: LocationViewModel = hiltViewModel(),
    onLocationClick: () -> Unit = {}
) {
//    Collect the state from viewModel
    val address by viewModel.userAddress.collectAsState()
    val deliveryTime by viewModel.deliveryTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

//    Trigger location update when the composable is first displayed or create
    LaunchedEffect(key1 = Unit) {
        viewModel.updateUserLocation()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        //    User icon
        IconButton(
            onClick = onLocationClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delivery In",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Text(
                    text = deliveryTime ?: "10 Mins",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            //    Address with dropdown
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Loading Address...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = address.ifEmpty { "Home - IIT market, Powai, Mumbai" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 2
                    )
                    if (isLoading && address.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(8.dp),
                            color = Color.White.copy(alpha = 0.5f),
                            strokeWidth = 1.dp
                        )
                    }
                }
            }
        }
    }
}