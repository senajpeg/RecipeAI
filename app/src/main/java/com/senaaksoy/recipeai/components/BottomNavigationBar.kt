package com.senaaksoy.recipeai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

            NavigationBarItem(
                selected = currentRoute == Screen.HomeScreen.route,
                onClick = { navController.navigate(Screen.HomeScreen.route) },
                icon = { Icon(Icons.Default.Home, contentDescription = null, tint = if(currentRoute == Screen.HomeScreen.route) selectedColor else unselectedColor) },
                label = { Text(stringResource(R.string.anasayfa), color = if(currentRoute == Screen.HomeScreen.route) selectedColor else unselectedColor) }
            )

            NavigationBarItem(
                selected = currentRoute == Screen.FavoritesScreen.route,
                onClick = { navController.navigate(Screen.FavoritesScreen.route) },
                icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = if(currentRoute == Screen.FavoritesScreen.route) selectedColor else unselectedColor) },
                label = { Text(stringResource(R.string.favorites), color = if(currentRoute == Screen.FavoritesScreen.route) selectedColor else unselectedColor) }
            )

            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate(Screen.AddRecipeScreen.route) },
                icon = {   Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFF64B5F6),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                } },
                label = { Text("") },
            )

            NavigationBarItem(
                selected = currentRoute == Screen.SearchScreen.route,
                onClick = { navController.navigate(Screen.SearchScreen.route) },
                icon = { Icon(Icons.Default.Search, contentDescription = null, tint = if(currentRoute == Screen.SearchScreen.route) selectedColor else unselectedColor) },
                label = { Text(stringResource(R.string.search), color = if(currentRoute == Screen.SearchScreen.route) selectedColor else unselectedColor) }
            )

            NavigationBarItem(
                selected = currentRoute == Screen.ProfileScreen.route,
                onClick = { navController.navigate(Screen.ProfileScreen.route) },
                icon = { Icon(Icons.Default.Person, contentDescription = null, tint = if(currentRoute == Screen.ProfileScreen.route) selectedColor else unselectedColor) },
                label = { Text(stringResource(R.string.profile), color = if(currentRoute == Screen.ProfileScreen.route) selectedColor else unselectedColor) }
            )
        }
    }
}

