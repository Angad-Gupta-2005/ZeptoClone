package com.angad.zeptoclone.ui.screens.payments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.models.payment.PaymentRecord
import com.angad.zeptoclone.ui.viewmodel.PaymentViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentSuccessScreen(
    paymentId: String,
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    onDone: () -> Unit
) {

    val buttonColor = Color(0xFFFF3F6C)
    val accentGreen = Color(0xFF0D7148)

    var payment by remember { mutableStateOf<PaymentRecord?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

//    Automatically navigate back after 5 seconds
    LaunchedEffect(Unit) {
        delay(5000)
        onDone()
    }

//    Load payment details from firebase
    LaunchedEffect(paymentId) {
        paymentViewModel.getPaymentDetails(
            paymentId = paymentId,
            onSuccess = { paymentRecord ->
                payment = paymentRecord
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                color = accentGreen
            )
        } else if (errorMessage.isNotEmpty()) {
            Text(
                text = "Error: $errorMessage",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //    Success checkmark icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = accentGreen,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.size(24.dp))

                Text(
                    text = "Payment Successful",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your order has been placed successfully and will be delivered in 10 minutes",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                //    Payment details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Amount Paid",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )

                            payment?.let {
                                Text(
                                    text = "₹${it.amount.toInt()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Payment ID",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )

                            Text(
                                text = paymentId,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Date & Time",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )

                            payment?.let {
                                val dateFormat =
                                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                Text(
                                    text = dateFormat.format(it.timeStamp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                //    Success animation or image
                Image(
                    painter = painterResource(R.drawable.baseline_delivery_dining_24),
                    contentDescription = "Delivery",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                //    Continue Button
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CONTINUE SHOPPING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}