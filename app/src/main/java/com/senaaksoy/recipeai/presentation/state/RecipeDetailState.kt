package com.senaaksoy.recipeai.presentation.state

import com.senaaksoy.recipeai.domain.model.Recipe

data class RecipeDetailState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false
)