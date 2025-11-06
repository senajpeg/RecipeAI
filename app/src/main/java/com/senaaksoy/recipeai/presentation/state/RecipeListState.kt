package com.senaaksoy.recipeai.presentation.state

import com.senaaksoy.recipeai.domain.model.Recipe

data class RecipeListState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)