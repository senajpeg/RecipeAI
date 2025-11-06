package com.senaaksoy.recipeai.presentation.state

data class AddRecipeState(
    val name: String = "",
    val description: String = "",
    val instructions: String = "",
    val cookingTime: String = "",
    val difficulty: String = "Kolay",
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)