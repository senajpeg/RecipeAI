package com.senaaksoy.recipeai.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    NavigationBar(containerColor = Color(0xFF0D47A1)) {
        NavigationBarItem(
            selected = currentRoute == Screen.HomeScreen.route,
            onClick = { navController.navigate(Screen.HomeScreen.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color.White) },
            label = { Text(stringResource(R.string.anasayfa), color = Color.White) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.FavoritesScreen.route,
            onClick = { navController.navigate(Screen.FavoritesScreen.route) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White) },
            label = { Text(stringResource(R.string.favorites), color = Color.White) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.AddRecipeScreen.route) },
            icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
            label = { Text("") },
            colors = NavigationBarItemColors(
                disabledTextColor = Color(0xFF4598BD),
                selectedIconColor = Color(0xFF4598BD),
                selectedTextColor = Color(0xFF4598BD),
                selectedIndicatorColor = Color(0xFF3A718D),
                unselectedIconColor = Color(0xFF4598BD),
                unselectedTextColor = Color(0xFF4598BD),
                disabledIconColor = Color(0xFF4598BD)
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.SearchScreen.route,
            onClick = { navController.navigate(Screen.SearchScreen.route) },
            icon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
            label = { Text(stringResource(R.string.search), color = Color.White) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.ProfileScreen.route,
            onClick = { navController.navigate(Screen.ProfileScreen.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
            label = { Text(stringResource(R.string.profile), color = Color.White) }
        )
    }
}
