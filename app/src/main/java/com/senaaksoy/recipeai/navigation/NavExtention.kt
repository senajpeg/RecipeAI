package com.senaaksoy.recipeai.navigation

import androidx.navigation.NavController

fun NavController.navigateSingleTopClear(route: String) {
    this.navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}