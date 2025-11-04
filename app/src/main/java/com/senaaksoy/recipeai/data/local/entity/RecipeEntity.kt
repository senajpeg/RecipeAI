package com.senaaksoy.recipeai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val instructions: String,
    val cookingTime: Int?,
    val difficulty: String?,
    val imageUrl: String?,
    val createdAt: Long = System.currentTimeMillis()
)