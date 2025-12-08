package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.remote.api.MealDbApi
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.TranslationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val backendApi: RecipeApiService,
    private val mealDbApi: MealDbApi,
    private val dao: RecipeDao,
    private val translationManager: TranslationManager
) : RecipeRepository {

    // Local'den Flow ile tarifleri dinle
    override fun getAllRecipesFromLocal(): Flow<List<Recipe>> {
        return dao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    // MealDB'den tarifleri çek ve çevir
    override suspend fun syncRecipesFromApi(): Resource<List<Recipe>> {
        return try {
            Log.d("RecipeRepository", "🔵 MealDB'den tarifler çekiliyor...")

            val categories = listOf("Chicken", "Beef", "Pasta", "Seafood", "Dessert")
            val allRecipes = mutableListOf<Recipe>()

            categories.forEach { category ->
                try {
                    Log.d("RecipeRepository", "📋 Kategori: $category")
                    val response = mealDbApi.getMealsByCategory(category)

                    if (response.isSuccessful && response.body()?.meals != null) {
                        val recipes = response.body()!!.meals
                            ?.take(3)
                            ?.mapNotNull { mealDto ->
                                try {
                                    val recipe = mealDto.toRecipe()

                                    // ⚠️ Çeviri hatasını yakala
                                    val translatedName = try {
                                        translationManager.translate(recipe.name)
                                    } catch (e: Exception) {
                                        Log.e("RecipeRepository", "⚠️ Çeviri hatası: ${e.message}")
                                        recipe.name // Çevrilemezse orijinal ismi kullan
                                    }

                                    recipe.copy(name = translatedName)
                                } catch (e: Exception) {
                                    Log.e("RecipeRepository", "❌ Tarif dönüştürme hatası: ${e.message}")
                                    null
                                }
                            } ?: emptyList()

                        allRecipes.addAll(recipes)
                        Log.d("RecipeRepository", "✅ $category: ${recipes.size} tarif eklendi")
                    } else {
                        Log.e("RecipeRepository", "❌ $category: Response başarısız")
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "❌ $category hatası: ${e.message}")
                }
            }

            if (allRecipes.isNotEmpty()) {
                Log.d("RecipeRepository", "✅ Toplam ${allRecipes.size} tarif yüklendi")
                Resource.Success(allRecipes)
            } else {
                Log.e("RecipeRepository", "❌ Hiç tarif yüklenemedi!")
                Resource.Error("Tarifler yüklenemedi")
            }

        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ FATAL: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Bağlantı hatası")
        }
    }

    override suspend fun getRecipeById(id: Int): Resource<Recipe> {
        return try {

            // ✅ GEMINI veya BACKEND TARİFİ
            if (id < 0) {
                Log.d("RecipeRepository", "✅ Backend tarifine gidiliyor: $id")

                val response = backendApi.getRecipeById(id)

                if (response.isSuccessful && response.body() != null) {
                    val recipe = response.body()!!.toRecipe()
                   return Resource.Success(recipe)
                } else {
                   return Resource.Error("Backend tarif getirilemedi")
                }

            }
            // ✅ MEALDB TARİFİ
            else {
                Log.d("RecipeRepository", "✅ MealDB tarifine gidiliyor: $id")

                val response = mealDbApi.getMealById(id.toString())

                if (response.isSuccessful && response.body()?.meals != null) {
                    val meal = response.body()!!.meals?.firstOrNull()
                    if (meal != null) {
                        val recipe = meal.toRecipe()
                        val translatedName = translationManager.translate(recipe.name)
                        val translatedInstructions = translationManager.translate(recipe.instructions)

                        Resource.Success(
                            recipe.copy(
                                name = translatedName,
                                instructions = translatedInstructions
                            )
                        )
                    } else {
                        Resource.Error("Tarif bulunamadı")
                    }
                } else {
                    Resource.Error("Tarif getirilemedi")
                }
            }

        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }


    // Arama yap ve çevir
    suspend fun searchRecipes(query: String): Resource<List<Recipe>> {
        return try {
            if (query.isBlank()) {
                return syncRecipesFromApi()
            }

            val response = mealDbApi.searchMeals(query)

            if (response.isSuccessful && response.body()?.meals != null) {
                val recipes = response.body()!!.meals
                    ?.mapNotNull { mealDto ->
                        try {
                            val recipe = mealDto.toRecipe()
                            val translatedName = translationManager.translate(recipe.name)
                            recipe.copy(name = translatedName)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                if (recipes.isNotEmpty()) {
                    Resource.Success(recipes)
                } else {
                    Resource.Error("'$query' için tarif bulunamadı")
                }
            } else {
                Resource.Error("Arama başarısız")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Arama hatası")
        }
    }


    suspend fun getRandomRecipes(count: Int = 3): Resource<List<Recipe>> {
        return try {
            Log.d("RecipeRepository", "🎲 $count adet random tarif çekiliyor...")
            val recipes = mutableListOf<Recipe>()

            repeat(count) { index ->
                try {
                    val response = mealDbApi.getRandomMeal()
                    if (response.isSuccessful && response.body()?.meals != null) {
                        response.body()!!.meals?.firstOrNull()?.let { mealDto ->
                            try {
                                val recipe = mealDto.toRecipe()

                                // ⚠️ Çeviri hatasını yakala
                                val translatedName = try {
                                    translationManager.translate(recipe.name)
                                } catch (e: Exception) {
                                    Log.e("RecipeRepository", "⚠️ Çeviri hatası: ${e.message}")
                                    recipe.name
                                }

                                recipes.add(recipe.copy(name = translatedName))
                                Log.d("RecipeRepository", "✅ Random tarif ${index + 1}: ${recipe.name}")
                            } catch (e: Exception) {
                                Log.e("RecipeRepository", "❌ Random tarif dönüştürme hatası", e)
                            }
                        }
                    } else {
                        Log.e("RecipeRepository", "❌ Random tarif ${index + 1}: Response başarısız")
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "❌ Random tarif ${index + 1} hatası: ${e.message}")
                }
            }

            if (recipes.isNotEmpty()) {
                Log.d("RecipeRepository", "✅ ${recipes.size} random tarif yüklendi")
                Resource.Success(recipes)
            } else {
                Log.e("RecipeRepository", "❌ Hiç random tarif yüklenemedi!")
                Resource.Error("Random tarifler yüklenemedi")
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ Random tarifler FATAL: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    // Local operations
    override suspend fun createRecipe(recipe: Recipe): Resource<Recipe> {
        return Resource.Error("Henüz desteklenmiyor")
    }

    override suspend fun updateRecipe(recipe: Recipe): Resource<Recipe> {
        return Resource.Error("Henüz desteklenmiyor")
    }

    override suspend fun deleteRecipe(recipe: Recipe): Resource<Unit> {
        return Resource.Error("Henüz desteklenmiyor")
    }
}