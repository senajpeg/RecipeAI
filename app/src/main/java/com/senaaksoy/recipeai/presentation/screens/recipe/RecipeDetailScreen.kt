package com.senaaksoy.recipeai.presentation.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.presentation.viewmodel.FavoriteViewModel
import com.senaaksoy.recipeai.presentation.viewmodel.RecipeDetailViewModel

@Composable
fun RecipeDetailScreen(
    navController: NavController,
    onRecipeLoaded: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val favoriteStates by favoriteViewModel.favoriteStates.collectAsState()

    LaunchedEffect(state.recipe) {
        state.recipe?.let { recipe ->
            onRecipeLoaded(recipe.name)
            favoriteViewModel.checkFavorite(recipe.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4A90E2),
                        Color(0xFF7B68EE),
                        Color(0xFFA5BBD0)
                    )
                )
            )
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error ?: stringResource(R.string.bir_hata_olustu),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigateUp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4A90E2)
                        )
                    ) {
                        Text(stringResource(R.string.geri_don))
                    }
                }
            }

            state.recipe != null -> {
                val recipe = state.recipe!!
                val isFavorite = favoriteStates[recipe.id] ?: false
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = recipe.imageUrl ?: R.drawable.tarif,
                            contentDescription = recipe.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )

                        IconButton(
                            onClick = { favoriteViewModel.toggleFavorite(recipe) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                if (recipe.cookingTime != null) {
                                    InfoChip(
                                        icon = Icons.Default.AccessTime,
                                        text = "${recipe.cookingTime} dk",
                                        backgroundColor = Color(0xFFE3F2FD)
                                    )
                                }
                                if (recipe.difficulty != null) {
                                    InfoChip(
                                        icon = Icons.Default.Star,
                                        text = recipe.difficulty,
                                        backgroundColor = Color(0xFFFFF9C4)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (!recipe.description.isNullOrBlank()) {
                                SectionTitle(stringResource(R.string.aciklama))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = recipe.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (!recipe.ingredients.isNullOrEmpty()) {
                                SectionTitle(stringResource(R.string.malzemelerr))
                                Spacer(modifier = Modifier.height(12.dp))

                                recipe.ingredients.forEach { ingredient ->
                                    IngredientItem(ingredient)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                            SectionTitle(stringResource(R.string.yapilisii))
                            Spacer(modifier = Modifier.height(12.dp))

                            val instructions = recipe.instructions
                                ?.split("\r\n", "\n")
                                ?.filter { it.isNotBlank() }
                                ?: emptyList()

                            instructions.forEachIndexed { index, instruction ->
                                InstructionStep(
                                    stepNumber = index + 1,
                                    instruction = instruction.trim()
                                )
                                if (index < instructions.size - 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF424242),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2)
    )
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        thickness = 2.dp,
        color = Color(0xFF1976D2).copy(alpha = 0.3f)
    )
}

@Composable
fun InstructionStep(stepNumber: Int, instruction: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1976D2),
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun IngredientItem(ingredient: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.tik),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))


        Text(
            text = ingredient,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f)
        )
    }
}