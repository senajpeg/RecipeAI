package com.senaaksoy.recipeai.data.remote.dto

import com.senaaksoy.recipeai.data.local.entity.RecipeEntity
import com.senaaksoy.recipeai.domain.model.Recipe

// DTO -> Domain Model
fun RecipeDto.toRecipe(): Recipe {
    return Recipe(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        createdAt = System.currentTimeMillis()
    )
}

// DTO -> Entity
fun RecipeDto.toEntity(): RecipeEntity {
    return RecipeEntity(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        createdAt = System.currentTimeMillis()
    )
}

// Entity -> Domain Model
fun RecipeEntity.toRecipe(): Recipe {
    return Recipe(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}

// Domain Model -> Entity
fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}