package com.senaaksoy.recipeai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.local.entity.IngredientEntity
import com.senaaksoy.recipeai.data.local.entity.RecipeEntity
import com.senaaksoy.recipeai.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        RecipeIngredientEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}