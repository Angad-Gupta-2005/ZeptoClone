package com.angad.zeptoclone.ui.screens.payments

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.models.payment.ItemDetails
import com.angad.zeptoclone.data.models.payment.PaymentRecord
import com.angad.zeptoclone.data.models.payment.PriceQuantityInfo
import com.angad.zeptoclone.ui.viewmodel.PaymentDetailState
import com.angad.zeptoclone.ui.viewmodel.PaymentDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    paymentId: String,
    navController: NavController,
    viewModel: PaymentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val zeptoPurple = Color(0xFF5D0079)

    LaunchedEffect(paymentId) {
        viewModel.loadPaymentDetail(paymentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = zeptoPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PaymentDetailState.Loading -> {
                    LoadingState()
                }

                is PaymentDetailState.Success -> {
                    ProfessionalPaymentDetailContent(payment = state.payment)
                }

                is PaymentDetailState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadPaymentDetail(paymentId) }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF5D0079),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading payment details...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = Color.Red.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Couldn't load payment details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5D0079)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Try Again",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ProfessionalPaymentDetailContent(payment: PaymentRecord) {
//    debug logging for image URLs
    LaunchedEffect(payment) {
        Log.d("PaymentDetailScreen", "ProfessionalPaymentDetailContent: has ${payment.items.size} ")
        Log.d(
            "PaymentDetailScreen",
            "Payment itemImageUrls: ${if (payment.itemImageUrls == null) "null" else "${payment.itemImageUrls?.size} entries"}"
        )

        //    log all image url
        payment.itemImageUrls.forEach { (key, url) ->
            Log.d("PaymentDetailScreen", "Image Urls for $key : $url ")
        }

        //    Check if the URLs are actually valid URLs
        payment.itemImageUrls.values.forEach { url ->
            try {
                val uri = java.net.URI(url)
                val isValidUrl = uri.scheme?.startsWith("http") == true
            } catch (e: Exception) {
                Log.e("PaymentDetailScreen", "Invalid url: $url ", e)
            }
        }

        //    Log the items to check matching
        payment.items.forEach { item ->
            val itemName = parseItemDetails(item).name
            val matchingKey = payment.itemImageUrls?.keys?.firstOrNull { key ->
                key.contains(itemName, ignoreCase = true) || itemName.contains(
                    key,
                    ignoreCase = true
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaymentStatusCard(payment)
        OrderDetailsCard(payment)
        ItemsListCard(payment)
    }
}

@Composable
fun PaymentStatusCard(payment: PaymentRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE8F2E9))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Payment Status",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.size(24.dp))

            //    Amount
            Text(
                text = "₹${payment.amount.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Total Amount Paid",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.size(16.dp))

            //    Payment time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_access_time_24),
                    contentDescription = null,
                    tint = Color(0xFF5D0079),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

                Text(
                    text = dateFormat.format(payment.timeStamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun OrderDetailsCard(payment: PaymentRecord) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Order Details",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            //    OrderID
            DetailRow(
                icon = Icons.Default.Check,
                label = "Order ID",
                value = payment.orderId
            )

            Spacer(modifier = Modifier.height(16.dp))

            //    Payment ID
            DetailRow(
                icon = Icons.Default.Menu,
                label = "Payment ID",
                value = payment.id
            )

            if (payment.userEmail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                //    User Email
                DetailRow(
                    icon = Icons.Default.Email,
                    label = "User Email",
                    value = payment.userEmail
                )
            }

            if (payment.userPhone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                //    User Phone
                DetailRow(
                    icon = Icons.Default.Phone,
                    label = "User Phone",
                    value = payment.userPhone
                )
            }

        }
    }

}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        7:54:25
        //    Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF5D0079),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun ItemsListCard(payment: PaymentRecord) {
    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(payment) {
        Log.d("ItemsListCard", "Item image url:  ${payment.itemImageUrls?.keys?.joinToString()} ")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring()),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items (${payment.items.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Show less" else "Show more",
                    tint = Color.Gray,
//                    modifier = Modifier.size(24.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(24.dp))

                if (payment.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items in this order",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    //    List items
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        payment.items.forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.height(8.dp),
                                    thickness = 1.dp,
                                    color = Color.Gray
                                )
                            }

                            val imageUrl = getImageUrlForItem(payment, item)

                            Log.d("ItemsListCard", "Item: $item, Found Url: $imageUrl ")

                            EnhancedItemRow(
                                item = item,
                                imageUrl = imageUrl
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getImageUrlForItem(payment: PaymentRecord, item: String): String? {
    if (payment.itemImageUrls == null || payment.itemImageUrls.isEmpty()) {
        return null
    }

//    Extract item name
    val itemDetails = parseItemDetails(item)
    val itemName = itemDetails.name.trim()

//    Strategy 1: Try to extract item ID from the beginning of the item string
    val idRegex = "^(\\d+):".toRegex()
    val idMatch = idRegex.find(item)

    if (idMatch != null) {
        val id = idMatch.groupValues[1]
        Log.d("ItemsListCard", "Trying to match with ID: $id")
        payment.itemImageUrls?.get(id)?.let {
            return it
        }
    }

//    Strategy 2: Try exact match with item name
    payment.itemImageUrls?.keys?.forEach { key ->
        if (key.equals(itemName, ignoreCase = true)) {
            return payment.itemImageUrls[key]
        }
    }

//    Strategy 3: Try to match first word of item name with keys
    val firstWord = itemName.split(" ").firstOrNull()
    if (!firstWord.isNullOrEmpty()) {
        payment.itemImageUrls?.keys?.forEach { key ->
            if (key.equals(firstWord, ignoreCase = true)) {
                return payment.itemImageUrls[key]
            }
        }
    }

//    Strategy 4: Check if the item name starts with any key that is a number
    val numericKeys = payment.itemImageUrls?.keys?.filter { it.matches(Regex("\\d+$")) }
    numericKeys?.forEach { key ->
        if (itemName.startsWith(key, ignoreCase = true)) {
            return payment.itemImageUrls[key]
        }
    }

//    Strategy 5: Known product IDs based on the item name
//    This is based on the fact that we see some pattern matching in our logs
    val knownProducts = mapOf(
        "Fjallraven - Foldsack No. 1 Backpack" to "1",
        "Mens Casual Premium Slim Fit T-Shirts" to "2",
        "Mens Cotton Jacket" to "3",
        "Mens Casual Slim Fit" to "4",
        "Opna Women's Short Sleeve Moisture" to "19",
        "Rosol" to "52904",
        "Keleya Zaara" to "53020",
        "Beef - Montreal Smoked Brisket" to "52904",
        "Kung Pao Chicken" to "52945"
    )

    for ((productName, id) in knownProducts) {
        if (itemName.contains(productName, ignoreCase = true)) {
            payment.itemImageUrls?.get(id)?.let {
                return it
            }
        }
    }

//    Strategy 6: For food items try to match with meal DB IDs
    val mealDbKeys = payment.itemImageUrls?.keys?.filter { it.matches(Regex("^5\\d+$")) }
    if (listOf("chicken", "beef", "fish", "soup", "meal", "food", "dish", "curry", "salad")
            .any { itemName.contains(it, ignoreCase = true) }
    ) {
        mealDbKeys?.firstOrNull()?.let { key ->
            return payment.itemImageUrls[key]
        }
    }

//    If all else fails, try to match with any available key as a last resort
    val keys = payment.itemImageUrls?.keys?.toList() ?: emptyList()
    if (keys.isNotEmpty()) {

        if (itemName.contains("Fjallraven", ignoreCase = true) && keys.contains("1")) {
            return payment.itemImageUrls["1"]
        }

        if (itemName.contains("Premium Slim", ignoreCase = true) && keys.contains("2")) {
            return payment.itemImageUrls["2"]
        }

        if (itemName.contains("Cotton Jacket", ignoreCase = true) && keys.contains("3")) {
            return payment.itemImageUrls["3"]
        }

        if (itemName.contains("Casual Slim Fit", ignoreCase = true) && keys.contains("4")) {
            return payment.itemImageUrls["4"]
        }

        if (itemName.contains("Women", ignoreCase = true) && keys.contains("19")) {
            return payment.itemImageUrls["19"]
        }

        //    Food items
        if (itemName.contains("Rosol", ignoreCase = true) && keys.contains("52904")) {
            return payment.itemImageUrls["52904"]
        }

        if (itemName.contains("Keleya", ignoreCase = true) && keys.contains("53020")) {
            return payment.itemImageUrls["53020"]
        }

        if (itemName.contains("Beef", ignoreCase = true) && keys.contains("52974")) {
            return payment.itemImageUrls["52974"]
        }

        if (itemName.contains("Chicken", ignoreCase = true) && keys.contains("52945")) {
            return payment.itemImageUrls["52945"]
        }

        //    Last resort - return the first url
        return payment.itemImageUrls[keys.first()]
    }
    return null
}

@Composable
fun EnhancedItemRow(
    item: String,
    imageUrl: String?
) {
    val itemDetails = parseItemDetails(item)
    val priceInfo = parsePriceAndQuantity(itemDetails.priceQuantity)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //    Product image with shadow and rounded corners
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .padding(4.dp)
        ) {
            ItemImage(
                imageUrl = imageUrl,
                item = item,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        //    Item Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = itemDetails.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            //    Price with quantity
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceInfo.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF212121)
                )

                Text(
                    text = " x ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                //    Quantity badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEFEBF3))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priceInfo.quantity,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5D0079)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                //    Sub total
                Text(
                    text = priceInfo.subTotal,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5D0079)
                )
            }

        }
    }
}

@Composable
fun ItemImage(
    imageUrl: String?,
    item: String,
    modifier: Modifier = Modifier
) {
    Log.d("ItemImage", "Rendering for item: $item with url: $imageUrl ")
    if (imageUrl.isNullOrEmpty()) {
        //    Fallback for missing image Url
        Box(
            modifier = modifier.background(getCategoryColor(item)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(item),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        val context = LocalContext.current

        Box(modifier = modifier.background(Color.White)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Item Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = {
                    Log.e("ItemImage", "Error loading image for item: $item", it.result.throwable)
                },
                placeholder = painterResource(R.drawable.placeholder_image)
            )
        }
    }
}

//    Helper function to extract price and quantity from item string
private fun parseItemDetails(itemString: String): ItemDetails {
//    Format is typically "Item Name (₹Price x Quantity = ₹Total)"
    val name = itemString.substringBefore("(").trim()
    val priceQuantityPart = if (itemString.contains("(")) {
        itemString.substringAfter("(").substringBefore(")")
    } else {
        ""
    }
    return ItemDetails(name, priceQuantityPart)
}

private fun parsePriceAndQuantity(priceQuantityText: String): PriceQuantityInfo {
//    Default value if parsing is fails
    var price = "₹0"
    var quantity = "₹0"
    var subTotal = "₹0"

    try {
        //    Example format: "₹120 x 2 = ₹240"
        val parts = priceQuantityText.split("x", "=")
        if (parts.size >= 3) {
            price = parts[0].trim()
            quantity = parts[1].trim()
            subTotal = parts[2].trim()
        }
    } catch (e: Exception) {
        Log.e("PaymentDetailScreen", "Error parsing price quantity: $priceQuantityText", e)
    }
    return PriceQuantityInfo(price, quantity, subTotal)
}

//    Get icon based on item name
private fun getCategoryIcon(item: String): ImageVector {
    return when {
        item.contains("rice", ignoreCase = true) ||
                item.contains("dal", ignoreCase = true) ||
                item.contains("flour", ignoreCase = true) -> Icons.Filled.ShoppingCart

        item.contains("fruit", ignoreCase = true) ||
                item.contains("apple", ignoreCase = true) ||
                item.contains("banana", ignoreCase = true) -> Icons.Filled.ShoppingCart

        item.contains("milk", ignoreCase = true) ||
                item.contains("curd", ignoreCase = true) ||
                item.contains("cheese", ignoreCase = true) -> Icons.Filled.ShoppingCart

        item.contains("bread", ignoreCase = true) ||
                item.contains("biscuit", ignoreCase = true) -> Icons.Filled.ShoppingCart

        item.contains("vegetable", ignoreCase = true) ||
                item.contains("tomato", ignoreCase = true) ||
                item.contains("potato", ignoreCase = true) -> Icons.Filled.ShoppingCart

        else -> Icons.Filled.ShoppingCart
    }
}

//    Get color based on item name 881
private fun getCategoryColor(item: String): Color {
    return when {
        item.contains("rice", ignoreCase = true) ||
                item.contains("dal", ignoreCase = true) ||
                item.contains("flour", ignoreCase = true) -> Color(0xFFE57373)

        item.contains("fruit", ignoreCase = true) ||
                item.contains("apple", ignoreCase = true) ||
                item.contains("banana", ignoreCase = true) -> Color(0xFFFFB74D)

        item.contains("milk", ignoreCase = true) ||
                item.contains("curd", ignoreCase = true) ||
                item.contains("cheese", ignoreCase = true) -> Color(0xFF4FC3F7)


        item.contains("bread", ignoreCase = true) ||
                item.contains("biscuit", ignoreCase = true) -> Color(0xFFBA68C8)

        item.contains("vegetable", ignoreCase = true) ||
                item.contains("tomato", ignoreCase = true) ||
                item.contains("potato", ignoreCase = true) -> Color(0xFF81C784)

        else -> Color(0xFF9E9E9E)
    }
}