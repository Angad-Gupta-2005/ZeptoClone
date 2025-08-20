package com.angad.zeptoclone.ui.screens.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.angad.zeptoclone.R
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

//  6:41:00  56
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSummaryBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    totalPrice: Double,
    itemCount: Int,
    tipAmount: Int,
    onApplyFreeDelivery: () -> Unit,
    isFreeDeliveryApplied: Boolean,
    isApplyingFreeDelivery: Boolean,
    finalTotal: Double,
    onPayClicked: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val MIN_CART_VAL_FOR_FREE_DELIVERY = 200.0
    val handlingCost = 14.99
    val gstOnHandling = 2.48
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val isLateNight = currentHour >= 23 || currentHour < 6
    val lateNightFee = if (isLateNight) 25.00 else 0.0
    val gstOnLateNight = if (isLateNight) 4.13 else 0.0
    var showInfoPopup by remember { mutableStateOf(false) }
    val deliveryFee = 30
    val itemTotalWithGst = totalPrice
    val discountedPrice = (itemTotalWithGst * 0.9).roundToInt()

    val isEligibleForFreeDelivery = remember(totalPrice) {
        derivedStateOf { totalPrice >= MIN_CART_VAL_FOR_FREE_DELIVERY }
    }

    val rawItemTotal = totalPrice
    val itemCost = rawItemTotal
    val exactItemTotal = itemCost + handlingCost + gstOnHandling + (if (isLateNight) gstOnLateNight else 0.0)
    val originalItemPrice = (itemCost * 1.1).roundToInt()

//    Use the pre-calculate finalTotal instead of recalculating here
    val totalWithTip = finalTotal

    val savings = remember(itemCost, isFreeDeliveryApplied) {
        val discountSavings = originalItemPrice - itemCost
        val deliverySavings = if (isFreeDeliveryApplied) deliveryFee.toDouble() else 0.0
        deliverySavings + discountSavings
    }

    if (isVisible){
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
            //    Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.receipt),
                        contentDescription = "Bill Info",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Bill Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

            //    Total item section with info button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Item Total & GST ($itemCount items)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )

                    //    Info button
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Price Info",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                                .clickable { showInfoPopup = true },
                            tint = Color(0xFF9E9E9E)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹$originalItemPrice",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.alpha(0.7f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "₹$exactItemTotal",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

            //    Delivery fee section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delivery Fee",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )

                    if (isFreeDeliveryApplied){
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹$deliveryFee",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.alpha(0.7f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "FREE",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Green,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "₹$deliveryFee",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            //    Late night handling fee if applicable
                if (isLateNight){
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Late Night Handling Charge",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "₹$lateNightFee",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

            //    Free delivery message
                if (!isFreeDeliveryApplied){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Free Delivery on this order",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isEligibleForFreeDelivery.value) Color.Green else Color.Gray
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (isEligibleForFreeDelivery.value){
                                    onApplyFreeDelivery()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF3F6C),
                                disabledContainerColor = Color.LightGray
                            ),
                            enabled = isEligibleForFreeDelivery.value && !isApplyingFreeDelivery,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            if (isApplyingFreeDelivery){
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else{
                                Text(
                                    text = "APPLY",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            //    Tip section
                if (tipAmount > 0){
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delivery Partner Tip",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )

                        Text(
                            text = "₹$tipAmount",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Green
                        )
                    }
                }

//                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
//                Spacer(modifier = Modifier.height(16.dp))

            //    Total to pay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "To Pay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Incl. all taxes and charges",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                    //    Display final amount with all charges
                        Text(
                            text = "₹${totalWithTip.roundToInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Saving ₹${savings.roundToInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            //    Payment Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()

                        //    Then launch payment
                            onPayClicked()  //  This will now call updated launchPaymentActivity
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3F6C)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PROCEED TO PAY ₹${finalTotal.roundToInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
        }

    //    Info popup for price details
        if (showInfoPopup){
            Dialog(
                onDismissRequest = { showInfoPopup = false}
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(16.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                    //    Header
                        Text(
                            text = "Zepto has no role to play in the taxes and charges being levied by the government",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                    //    Dynamic item cost from user's cart
                        PriceRow(
                            label = "Item Cost",
                            amount = itemCost
                        )

                    //    Fixed handling fees
                        PriceRow(
                            label = "Item Handling Cost",
                            amount = handlingCost
                        )

                    //    GST on handling
                        PriceRow(
                            label = "GST on Item handling Cost",
                            amount = gstOnHandling
                        )

                    //    Late night handling charge GST (if applicable)
                        if (isLateNight){
                            PriceRow(
                                label = "GST on Late Night Handling Charge",
                                amount = gstOnLateNight
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Color.LightGray
                        )

                    //    Total - use calculate total from item cost and fixed fees
                        PriceRow(
                            label = "Item Total & GST",
                            amount = gstOnLateNight,
                            isBold = true
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun PriceRow(
    label: String,
    amount: Double,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.DarkGray
        )

        Text(
            text = "₹${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
