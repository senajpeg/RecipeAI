package com.senaaksoy.recipeai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.local.entity.RecipeEntity

@Database(
    entities = [
        RecipeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}