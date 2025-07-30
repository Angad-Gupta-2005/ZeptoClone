package com.angad.zeptoclone.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.angad.zeptoclone.R
import com.angad.zeptoclone.data.models.fakeApi.Category
import com.angad.zeptoclone.data.models.fakeApi.Product
import com.angad.zeptoclone.ui.navigation.ZeptoDestinations
import com.angad.zeptoclone.ui.screens.components.CustomCircularProgressIndicator
import com.angad.zeptoclone.ui.screens.components.LocationBar
import com.angad.zeptoclone.ui.screens.components.NotFoundSection
import com.angad.zeptoclone.ui.screens.components.ProductCard
import com.angad.zeptoclone.ui.screens.components.SearchBar
import com.angad.zeptoclone.ui.screens.components.TrendingProductsSection
import com.angad.zeptoclone.ui.viewmodel.CartViewModel
import com.angad.zeptoclone.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMotionApi::class)
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    onNavigateToCategory: (String) -> Unit,
    navController: NavController,
    onProductClick: (Int) -> Unit = {}
) {

    val viewModel: HomeViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()
    val scrollState = rememberLazyListState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

//    Memorize product grid to prevent recalculation during scrolling
    val productGrid = remember(products) {
        products.chunked(3)
    }

//    Set default category
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory == null) {
            val allCategory = categories.find { it.name.equals("All", ignoreCase = true) }
                ?: categories.first()
            viewModel.selectCategory(allCategory)
        }
    }

//    Calculate scroll progress with optimization for first visible item
    val scrollProgress by remember {
        derivedStateOf {
            when {
                scrollState.firstVisibleItemIndex > 0 -> 1f
                scrollState.firstVisibleItemScrollOffset > 0 -> {
                    (scrollState.firstVisibleItemScrollOffset / 1000f).coerceIn(0f, 1f)
                }
                else -> 0f
            }
        }
    }

//    Faster animation for smoother performance
    val animatedScrollProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = tween(
            durationMillis = 50,
            easing = FastOutLinearInEasing
        ),
        label = "scroll"
    )

//    More stable category background memoization
    val categoryBackground = remember(selectedCategory?.id) {
        selectedCategory?.let {
            getCategoryGradient(it)
        } ?: Brush.horizontalGradient(listOf(Color.White, Color.White))
    }

//    Track if we need expensive calculations based on scroll position
    var isPastThreshold by remember { mutableStateOf(false) }

    LaunchedEffect(scrollProgress) {
        val newValue = scrollProgress > 0.25f
        if (isPastThreshold != newValue) {
            isPastThreshold = newValue
        }
    }

//    Main UI with hardware acceleration optimizations  178

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(categoryBackground)
    ) {
        MotionLayout(
            start = homeScreenStartConstraintSet(),
            end = homeScreenEndConstraintSet(),
            progress = animatedScrollProgress,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    renderEffect = null
                    alpha = 0.99f
                }
        ) {
            //    Location Bar 199
            Box(
                modifier = Modifier
                    .layoutId("location_bar")
                    .background(Color.Transparent)
                    .zIndex(0.9f)
            ) {
                LocationBar(contentColor = Color.White)
            }

            //    Search bar
            Box(
                modifier = Modifier
                    .layoutId("search_bar")
                    .background(Color.Transparent)
                    .zIndex(0.9f)
            ) {
                SearchBar(
                    onSearchClick = {
                        navController.navigate(ZeptoDestinations.Search.route)
                    }
                )
            }

            //    Categories section
            Box(
                modifier = Modifier
                    .layoutId("category_section")
                    .background(Color.Transparent)
                    .zIndex(0.9f)
            ) {
                CategoriesSection(
                    categories = categories,
                    onCategorySelected = viewModel::selectCategory,  // Passing a function as an argument
                    selectedCategory = selectedCategory
                )
            }

            //    Main content area start here
            val cornerShape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) }
            Box(
                modifier = Modifier
                    .layoutId("main_content")
                    .background(Color.White, cornerShape)
                    .clip(cornerShape)
                    .zIndex(0.7f)
            ) {
                if (isLoading && products.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomCircularProgressIndicator()
                    }
                } else if (error != null && products.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(error ?: "Unknown error occurred")
                    }
                } else {
                    //    Highly optimized product list
                    OptimizedProductList(
                        scrollState = scrollState,
                        productGrid = productGrid,
                        products = products,
                        onNavigateToCategory = onNavigateToCategory,
                        viewModel = viewModel,
                        cartViewModel = cartViewModel,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.let {
                it.index >= scrollState.layoutInfo.totalItemsCount - 5
            } ?: false
        }.distinctUntilChanged()
            .filter { it }
            .collect {
                //    Implement your pagination logic here if needed
            }
    }
}

@Composable
private fun OptimizedProductList(
    scrollState: LazyListState,
    productGrid: List<List<Product>>,
    products: List<Product>,
    onNavigateToCategory: (String) -> Unit,
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    onProductClick: (Int) -> Unit = {}
) {
    val context = LocalContext.current

//    Create image loader instance
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

//    Create sad emoji image request
    val sadEmojiRequest = remember {
        ImageRequest.Builder(context)
            .data(R.raw.sad_face)
            .crossfade(true)
            .build()
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) {
//        Trending section with fixed animation
        item(key = "trending_section") {
            Box(
                modifier = Modifier.animateItem()   //  animateItemPlacement() is deprecated
            ) {
                TrendingProductsSection(
                    products = products,
                    viewModel = viewModel,
                    onNavigateToCategory = onNavigateToCategory,
                )
            }
        }

//        Product grid with better keying strategy
        items(
            items = productGrid,
            key = { row ->
                "row-" + (row.firstOrNull()?.id?.toString() ?: row.hashCode().toString())
            }
        ) { rowProducts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .animateItem(fadeInSpec = null, fadeOutSpec = null),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for(product in rowProducts){
                    Box(
                        modifier = Modifier.weight(1f)
                    ){
                    //    Use key to prevent unnecessary recompositions
                        key(product.id) {
                            ProductCard(
                                product = product,
                                cartViewModel = cartViewModel,
                                onProductClick = onProductClick
                            )
                        }
                    }
                }
            //    Fill empty spots with blank spaces
                repeat(3 - rowProducts.size){
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

    //    Not found section at the end of the list
        item(key = "not_found_section"){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .animateItem()
            ){
                NotFoundSection(
                    imageLoader = imageLoader,
                    sadEmojiRequest = sadEmojiRequest,
                    onRetry = {  /* Add retry logic here if needed */}
                )
            }
        }
    }
}

//    221
fun getCategoryGradient(category: Category): Brush =
    when (category.name.lowercase()) {
        "jewelery" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF9D6300),
                Color(0xFFFDD38F)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )

        "electronics" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF012654),
                Color(0xFF236ECC)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )

        "women's clothing" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF9D0037),
                Color(0xFFCE5781)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )

        "men's clothing" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F6403),
                Color(0xFF6CEC4B)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )

        "all" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF021752),
                Color(0xFF263DD9)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )

        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFA2330C),
                Color(0xFFCB6642)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }

//    Home screen constraint set 404
fun homeScreenStartConstraintSet(): ConstraintSet =
    ConstraintSet {
        val locationBar = createRefFor("location_bar")
        val searchBar = createRefFor("search_bar")
        val categorySection = createRefFor("category_section")
        val mainContent = createRefFor("main_content")

        constrain(locationBar) {
            width = Dimension.fillToConstraints
            height = Dimension.value(95.dp)
            top.linkTo(parent.top, margin = 30.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(searchBar) {
            width = Dimension.fillToConstraints
            height = Dimension.value(60.dp)
            top.linkTo(locationBar.bottom, margin = (-12).dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(categorySection) {
            width = Dimension.fillToConstraints
            height = Dimension.value(100.dp)
            top.linkTo(searchBar.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(mainContent) {
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
            top.linkTo(categorySection.bottom, margin = (-17).dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

    }


fun homeScreenEndConstraintSet(): ConstraintSet =
    ConstraintSet {
        val locationBar = createRefFor("location_bar")
        val searchBar = createRefFor("search_bar")
        val categorySection = createRefFor("category_section")
        val mainContent = createRefFor("main_content")

        constrain(locationBar) {
            width = Dimension.fillToConstraints
            height = Dimension.value(75.dp)
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            alpha = 0f
        }

        constrain(searchBar) {
            width = Dimension.fillToConstraints
            height = Dimension.value(70.dp)
            top.linkTo(locationBar.bottom, margin = 40.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(categorySection) {
            width = Dimension.fillToConstraints
            height = Dimension.value(100.dp)
            top.linkTo(searchBar.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(mainContent) {
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
            top.linkTo(categorySection.bottom, margin = (-17).dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        }
    }

