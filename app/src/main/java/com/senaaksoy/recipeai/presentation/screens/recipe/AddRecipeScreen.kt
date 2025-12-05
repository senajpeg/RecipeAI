package com.senaaksoy.recipeai.presentation.screens.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.presentation.viewmodel.AddRecipeViewModel
import com.senaaksoy.recipeai.presentation.viewmodel.FavoriteViewModel

@Composable
fun AddRecipeScreen(
    navController: NavController,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var ingredientInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🤖 AI Tarif Oluşturucu",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Elindeki malzemeleri gir, AI sana tarif oluştursun!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Malzeme ekleme kartı
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "📝 Malzemelerini Ekle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Malzeme input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = ingredientInput,
                                onValueChange = { ingredientInput = it },
                                placeholder = { Text("Örn: Domates, Peynir...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF667EEA),
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (ingredientInput.isNotBlank()) {
                                        viewModel.addIngredient(ingredientInput.trim())
                                        ingredientInput = ""
                                    }
                                },
                                containerColor = Color(0xFF667EEA),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ekle",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Eklenen malzemeler listesi
            item {
                AnimatedVisibility(
                    visible = state.ingredients.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "🥗 Malzemeler (${state.ingredients.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667EEA)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            state.ingredients.forEach { ingredient ->
                                IngredientChip(
                                    text = ingredient,
                                    onDelete = { viewModel.removeIngredient(ingredient) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Generate butonu
            item {
                Button(
                    onClick = { viewModel.generateRecipe() },
                    enabled = state.ingredients.isNotEmpty() && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF667EEA)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI Tarif Oluşturuyor...", color = Color(0xFF667EEA))
                    } else {
                        Text(
                            "✨ Tarif Oluştur",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA)
                        )
                    }
                }
            }

            // Oluşturulan tarif
            item {
                state.generatedRecipe?.let { recipe ->
                    GeneratedRecipeCard(recipe)
                }
            }

            // Error mesajı
            item {
                state.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = "❌ $error",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientChip(text: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3E5F5))
            .border(1.dp, Color(0xFF667EEA), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $text",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF667EEA),
            fontWeight = FontWeight.Medium
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Sil",
                tint = Color(0xFF667EEA),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun GeneratedRecipeCard(
    recipe: com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe,
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val favoriteStates by favoriteViewModel.favoriteStates.collectAsState()

    val recipeModel = Recipe(
        id = recipe.name.hashCode(),
        name = recipe.name,
        description = recipe.description,
        instructions = recipe.instructions,
        cookingTime = recipe.cookingTime,
        difficulty = recipe.difficulty,
        imageUrl = null,
        ingredients = recipe.ingredients
    )

    val isFavorite = favoriteStates[recipeModel.id] ?: false

    LaunchedEffect(recipeModel.id) {
        favoriteViewModel.checkFavorite(recipeModel.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFF8F9FF))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF667EEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🍳",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                // FAVORİ YILDIZ
                IconButton(
                    onClick = { favoriteViewModel.toggleFavorite(recipeModel) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(icon = "⏱️", label = "${recipe.cookingTime} dk")
                InfoChip(icon = "📊", label = recipe.difficulty)
                InfoChip(icon = "🥘", label = "${recipe.ingredients.size} malzeme")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "📝 Malzemeler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA)
            )
            Spacer(modifier = Modifier.height(12.dp))
            recipe.ingredients.forEach { ingredient ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•", color = Color(0xFF667EEA), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ingredient,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "👨‍🍳 Yapılışı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recipe.instructions,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f)
            )

            if (recipe.suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Tavsiyeler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6F00)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        recipe.suggestions.forEach { suggestion ->
                            Text(
                                text = "→ $suggestion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }

            if (!recipe.canBeMade) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = "⚠️ Bu tarifin tam olarak yapılabilmesi için yukarıdaki önerilere ihtiyacınız olabilir.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: String, label: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF667EEA)
            )
        }
    }
}