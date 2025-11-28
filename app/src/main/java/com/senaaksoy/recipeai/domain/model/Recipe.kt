package com.senaaksoy.recipeai.domain.model

data class Recipe(
    val id: Int,
    val name: String,
    val description: String?,
    val instructions: String,
    val cookingTime: Int?,
    val difficulty: String?,
    val imageUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val ingredients: List<String> = emptyList()
)