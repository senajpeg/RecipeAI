package com.senaaksoy.recipeai.presentation.screens.favorite

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.presentation.viewmodel.FavoriteViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("FavoritesScreen", "🎬 Screen açıldı, loadFavorites() çağrılıyor")
        viewModel.loadFavorites()
    }

    LaunchedEffect(favorites) {
        Log.d("FavoritesScreen", "📊 Favorites listesi güncellendi: ${favorites.size} adet")
        favorites.forEach {
            Log.d("FavoritesScreen", "  - ${it.name} (ID: ${it.id})")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFD700),
                        Color(0xFFFFA500)
                    )
                )
            )
    ) {
        if (favorites.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Henüz favori tarifin yok",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Beğendiğin tarifleri favorilere ekle",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites, key = { it.id }) { recipe ->
                    FavoriteRecipeCard(
                        recipe = recipe,
                        onClick = {
                            Log.d("FavoritesScreen", "🔗 Tarif detayına gidiliyor: ${recipe.name}")
                            navController.navigate(
                                Screen.createRecipeDetailRoute(recipe.id)
                            )
                        },
                        onRemove = {
                            Log.d("FavoritesScreen", "🗑️ Silme isteği: ${recipe.name}")
                            viewModel.toggleFavorite(recipe)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Favoriden Çıkar",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
            },
            text = {
                Text(
                    text = "${recipe.name} tarifini favorilerden çıkarmak istediğinize emin misiniz?",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("FavoriteRecipeCard", "✅ Silme onaylandı: ${recipe.name}")
                        onRemove()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Çıkar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Log.d("FavoriteRecipeCard", "❌ Silme iptal edildi")
                    showDeleteDialog = false
                }) {
                    Text("İptal", color = Color(0xFF667EEA))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = recipe.imageUrl ?: R.drawable.tarif,
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA),
                    maxLines = 2
                )

                recipe.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipe.cookingTime?.let {
                        Chip(text = "$it dk", icon = "⏱️")
                    }
                    recipe.difficulty?.let {
                        Chip(text = it, icon = "📊")
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favori",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(28.dp)
                )

                IconButton(
                    onClick = {
                        Log.d("FavoriteRecipeCard", "🗑️ Silme dialogu açılıyor: ${recipe.name}")
                        showDeleteDialog = true
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Favoriden Çıkar",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, icon: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF3E5F5)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667EEA)
            )
        }
    }
}