package com.angad.zeptoclone.ui.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.angad.zeptoclone.R
import com.angad.zeptoclone.ui.viewmodel.CartViewModel

@Composable
fun DeliveryPartnerTipSection(
    selectedTip: Int,
    onTipSelected: (Int) -> Unit,
    accentGreen: Color = Color(0xFF0D7148),
    cartViewModel: CartViewModel? = null
) {

//    Define the tip option
    val tipOptions = listOf(20, 30, 50, 0)
    val lightGreen = Color(0xFFECFDF3)

    LaunchedEffect(selectedTip) {
        cartViewModel?.setTipAmount(if (selectedTip > 0) selectedTip else 0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
    //    Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.money),
                    contentDescription = "Tip",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Delivery Partner Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "This amount goes to delivery partner",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

    //    Tip options
        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tipOptions.forEach { tipAmount ->
                val isSelected = tipAmount == selectedTip

            //    Don't show the button for 0 tip, instead show "No Tip" option
                if (tipAmount == 0){
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val newTip = if (selectedTip == 0) -1 else 0
                                onTipSelected(newTip)
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) lightGreen else Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) accentGreen else Color.LightGray
                        )
                    ) {
                        Text(
                            text = "No Tip",
                            color = if (isSelected) accentGreen else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                } else {
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val newTip = if (selectedTip == tipAmount) -1 else tipAmount
                                onTipSelected(newTip)
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) lightGreen else Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) accentGreen else Color.LightGray
                        )
                    ){
                        Text(
                            text = "₹$tipAmount",
                            color = if (isSelected) accentGreen else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

    //    Show selected tip message
        if (selectedTip > 0){
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Thank you for tipping ₹$selectedTip to your delivery partner!",
                style = MaterialTheme.typography.bodyMedium,
                color = accentGreen
            )
        }
    }
}