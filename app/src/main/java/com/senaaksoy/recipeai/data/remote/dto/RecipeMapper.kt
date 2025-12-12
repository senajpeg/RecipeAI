package com.senaaksoy.recipeai.data.remote.dto

import com.senaaksoy.recipeai.data.local.entity.RecipeEntity
import com.senaaksoy.recipeai.domain.model.Recipe

// DTO -> DOMAIN
fun RecipeDto.toRecipe(): Recipe {
    return Recipe(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        createdAt = System.currentTimeMillis(),
        ingredients = ingredients ?: emptyList()
    )
}


// ENTITY -> DOMAIN
fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        createdAt = createdAt,
        ingredients = ingredients
    )
}

// DOMAIN -> ENTITY
fun Recipe.toEntity(isFavorite: Boolean = false): RecipeEntity {
    return RecipeEntity(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        ingredients = ingredients,
        createdAt = createdAt,
        isFavorite = isFavorite
    )
}
