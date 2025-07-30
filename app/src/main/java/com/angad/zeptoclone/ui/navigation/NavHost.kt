package com.angad.zeptoclone.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.angad.zeptoclone.ui.screens.HomeScreen
import com.angad.zeptoclone.ui.viewmodel.AuthViewModel
import com.angad.zeptoclone.ui.viewmodel.CategoryViewModel
import com.angad.zeptoclone.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ZeptoNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onNavGraphInitialized: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val products by productViewModel.products.collectAsState()
    var isAuthLoading by remember { mutableStateOf(true) }

    val (selectedProductId, setSelectedProductId) = remember { mutableStateOf<Int?>(null) }
    val selectedProduct = selectedProductId?.let { id -> products.find { it.id == id }  }

//    Determine the start screen based on the authentication state
    val startDestination = if (isAuthenticated){
        ZeptoDestinations.Welcome.route
    } else {
        ZeptoDestinations.Auth.route
    }

    LaunchedEffect(Unit) {
        delay(200)
        authViewModel.checkAuthStatus()
    //    Mark loading as complete
        isAuthLoading = false
        Log.d("ZeptoNavGraph", "Initial auth check complete: isAuthenticated = $isAuthenticated ")
    }

    LaunchedEffect(isAuthenticated) {
        Log.d("ZeptoNavGraph", "Auth state changed in NavGraph: = $isAuthenticated ")
        if (navController.currentBackStackEntry != null){
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            Log.d("ZeptoNavGraph", "Current route: $currentRoute")
            
            if (isAuthenticated && currentRoute == ZeptoDestinations.Auth.route){
            //    Navigate to welcome screen if authenticated but on auth screen
                navController.navigate(ZeptoDestinations.Welcome.route){
                    popUpTo(ZeptoDestinations.Auth.route){ inclusive = true }
                }
                onBottomBarVisibilityChange(false)  //  Hide bottom bar on welcome screen
            }
        }
    }

//    Main navigation host
    Box(modifier = Modifier.fillMaxSize()){
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ){

            composable(ZeptoDestinations.Welcome.route){
                LaunchedEffect(Unit) {
                    onBottomBarVisibilityChange(false)
                }
                WelcomeScreen(
                    navController = navController,
                    onCategorySelected = { category ->
                        when(category){
                            CategoryType.EVERYDAY -> {
                            //    Update: Don't pop the welcome screen to allow returning to it
                                navController.navigate(ZeptoDestinations.Home.route){
                                    //  Remove popUpTo to keep welcome screen in back stack
                                }
                                onBottomBarVisibilityChange(true)
                            }
                            CategoryType.CAFE -> {
                                navController.navigate(ZeptoDestinations.Cafe.route){
                                    //  Remove popUpTo to keep welcome screen in back stack
                                }
                                onBottomBarVisibilityChange(false)
                            }
                        }
                    }
                )
            }

        //    Cafe Screen
            composable(ZeptoDestinations.Cafe.route){
                CafeScreen(
                    paddingValues = paddingValues,
                    navController = navController
                )
            }

        //    Auth screen
            composable(ZeptoDestinations.Auth.route){
                LaunchedEffect(Unit) {
                    onBottomBarVisibilityChange(false)
                }
                AuthScreen(
                    onAuthSuccess  = {
                        navController.navigate(ZeptoDestinations.Welcome.route){
                            popUpTo(ZeptoDestinations.Auth.route){ inclusive = true }
                        }
                        onBottomBarVisibilityChange(true)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }

        //    Home screen
            composable(ZeptoDestinations.Home.route){
                HomeScreen(
                    paddingValues = PaddingValues(),
                    onNavigateToCategory = { categoryId ->
                        navController.navigate(ZeptoDestinations.CategoryDetail.createRoute(categoryId))
                    },
                    navController = navController,
                    onProductClick = { productId ->
                        setSelectedProductId(productId)
                    }
                )
            }

        //    Search screen
            composable(ZeptoDestinations.Search.route){
                LaunchedEffect(Unit) {
                    onBottomBarVisibilityChange(false)
                }
                SearchScreen(
                    paddingValues = PaddingValues(),
                    navController = navController,
                    onNavigateBack = {
                        navController.navigateUp()
                        onBottomBarVisibilityChange(true)
                    }
                )
            }

        //    Cart screen 202
        }
    }


}


