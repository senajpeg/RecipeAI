package com.senaaksoy.recipeai.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.senaaksoy.recipeai.R
import com.senaaksoy.recipeai.components.EditTextField
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.navigation.Screen
import com.senaaksoy.recipeai.presentation.viewmodel.RecipeListViewModel
import kotlinx.coroutines.delay

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val dailySuggestions by viewModel.dailySuggestions.collectAsState()
    val discoverRecipes by viewModel.discoverRecipes.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFA5BBD0), Color(0xFFD6D6E8))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.anasayfa),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            EditTextField(
                value = "",
                onValueChange = {},
                label = android.R.string.search_go,
                keyboardOptions = KeyboardOptions.Default,
                shape = RoundedCornerShape(20.dp),
                supportingText = { Text(stringResource(R.string.search_recipe), color = Color.White) },
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {navController.navigate(Screen.AddRecipeScreen.route) },
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(stringResource(R.string.yapay_zeka_ile_tarif_olustur))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.daily_suggest),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val result = dailySuggestions) {
                is Resource.Loading -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(3) { PlaceholderRecipeCard(160.dp, 120.dp) }
                    }
                }

                is Resource.Success -> {
                    val recipes = result.data ?: emptyList()
                    val listState = rememberLazyListState()
                    var visibleIndex by remember { mutableIntStateOf(0) }

                    // Auto-scroll + fade-in loop
                    LaunchedEffect(recipes) {
                        while (true) {
                            delay(2200)
                            val total = recipes.size
                            if (total > 0) {
                                val nextIndex = (visibleIndex + 1).mod(total)
                                listState.animateScrollToItem(nextIndex)
                                visibleIndex = nextIndex
                            }
                        }
                    }

                    LazyRow(
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recipes.size) { index ->
                            val recipe = recipes[index]
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(durationMillis = 600)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 600))
                            ) {
                                DailySuggestionCard(recipe) {
                                    navController.navigate(Screen.createRecipeDetailRoute(recipe.id))
                                }
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(3) { PlaceholderRecipeCard(160.dp, 120.dp) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.discover_recipe),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val result = discoverRecipes) {
                is Resource.Loading -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.7f)
                    ) {
                        items(10) { RecipeBoxPlaceholder() }
                    }
                }

                is Resource.Success -> {
                    val recipes = result.data ?: emptyList()
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.7f)
                    ) {
                        items(recipes) { recipe ->
                            RecipeBoxCard(recipe) {
                                navController.navigate(Screen.createRecipeDetailRoute(recipe.id))
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.7f)
                    ) {
                        items(10) { RecipeBoxPlaceholder() }
                    }
                }
            }
        }
    }
}

@Composable
fun DailySuggestionCard(recipe: Recipe, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(160.dp, 120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = recipe.imageUrl,
            contentDescription = recipe.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        Text(
            text = recipe.name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}

@Composable
fun RecipeBoxCard(recipe: Recipe, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        AsyncImage(
            model = recipe.imageUrl,
            contentDescription = recipe.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height((150..280).random().dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlaceholderRecipeCard(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
    )
}

@Composable
fun RecipeBoxPlaceholder() {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B294D))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.tarif_adi), style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(navController = rememberNavController())
}