package com.senaaksoy.recipeai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.senaaksoy.recipeai.data.local.converter.StringListConverter

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String?,
    val instructions: String,
    val cookingTime: Int?,
    val difficulty: String?,
    val imageUrl: String?,
    @TypeConverters(StringListConverter::class)
    val ingredients: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)