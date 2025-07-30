package com.angad.zeptoclone.ui.navigation

sealed class ZeptoDestinations(val route: String) {

    data object Auth : ZeptoDestinations("auth")

    data object Home : ZeptoDestinations("home")

    data object Search : ZeptoDestinations("search")

    data object Cart : ZeptoDestinations("cart")

    data object Account : ZeptoDestinations("account")

    data object Welcome : ZeptoDestinations("welcome")

    data object Cafe : ZeptoDestinations("cafe")


    data object CategoryDetail : ZeptoDestinations("category/{categoryId}") {
        fun createRoute(categoryId: String): String {
            return "category/$categoryId"
        }
    }

    //    Product details screen with dynamic productId parameter
    data object ProductDetail : ZeptoDestinations("product/{productId}") {
        fun createRoute(productId: String): String {
            return "product/$productId"
        }
    }
}