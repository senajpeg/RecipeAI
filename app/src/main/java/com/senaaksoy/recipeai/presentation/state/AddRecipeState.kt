package com.senaaksoy.recipeai.presentation.state

import com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe

data class AddRecipeState(
    val name: String = "",
    val description: String = "",
    val instructions: String = "",
    val cookingTime: String = "",
    val difficulty: String = "Kolay",
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val ingredients: List<String> = emptyList(),
    val generatedRecipe: AiGeneratedRecipe? = null
)