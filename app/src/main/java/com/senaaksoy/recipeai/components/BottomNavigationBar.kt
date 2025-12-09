package com.senaaksoy.recipeai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                )
            )
    ) {
        NavigationBar(containerColor = Color.Transparent) {
            val selectedColor = Color(0xFF1565C0)
            val unselectedColor = Color.White

            // 🏠 Ana Sayfa
            NavigationBarItem(
                selected = currentRoute == Screen.HomeScreen.route,
                onClick = { navController.navigate(Screen.HomeScreen.route) },
                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = if (currentRoute == Screen.HomeScreen.route) selectedColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.anasayfa),
                        color = if (currentRoute == Screen.HomeScreen.route) selectedColor else unselectedColor
                    )
                }
            )

            // ⭐ Favoriler
            NavigationBarItem(
                selected = currentRoute == Screen.FavoritesScreen.route,
                onClick = { navController.navigate(Screen.FavoritesScreen.route) },
                icon = {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (currentRoute == Screen.FavoritesScreen.route) selectedColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.favorites),
                        color = if (currentRoute == Screen.FavoritesScreen.route) selectedColor else unselectedColor
                    )
                }
            )

            // 👤 Profil
            NavigationBarItem(
                selected = currentRoute == Screen.ProfileScreen.route,
                onClick = { navController.navigate(Screen.ProfileScreen.route) },
                icon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (currentRoute == Screen.ProfileScreen.route) selectedColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.profile),
                        color = if (currentRoute == Screen.ProfileScreen.route) selectedColor else unselectedColor
                    )
                }
            )
        }
    }
}