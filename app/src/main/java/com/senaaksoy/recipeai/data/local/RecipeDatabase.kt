package com.senaaksoy.recipeai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.senaaksoy.recipeai.data.local.converter.StringListConverter
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.local.entity.RecipeEntity

@Database(
    entities = [RecipeEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}