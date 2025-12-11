package com.senaaksoy.recipeai.data.remote.dto

import com.senaaksoy.recipeai.data.local.entity.RecipeEntity
import com.senaaksoy.recipeai.domain.model.Recipe

// ✅ DTO -> DOMAIN (FAVORİ + DETAY EKRANI İÇİN)
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

// ✅ DTO -> ENTITY (ROOM KAYIT)
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

// ✅ ENTITY -> DOMAIN (ROOM'DAN OKUMA)
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

// ✅ DOMAIN -> ENTITY (ROOM'A YAZMA)
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
fun AiGeneratedRecipe.toRecipe(): Recipe {
    return Recipe(
        id = name.hashCode(),
        name = name,
        description = description,
        instructions = instructions,
        cookingTime = cookingTime,
        difficulty = difficulty,
        imageUrl = null,
        createdAt = System.currentTimeMillis(),
        ingredients = ingredients
    )
}