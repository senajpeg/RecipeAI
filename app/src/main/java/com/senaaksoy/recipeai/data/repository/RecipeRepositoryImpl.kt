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

    override fun getAllRecipesFromLocal(): Flow<List<Recipe>> {
        return dao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

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
                                    val translatedName = try {
                                        translationManager.translate(recipe.name)
                                    } catch (e: Exception) {
                                        Log.e("RecipeRepository", "⚠️ İsim çeviri hatası: ${e.message}")
                                        recipe.name
                                    }
                                    recipe.copy(name = translatedName)
                                } catch (e: Exception) {
                                    Log.e("RecipeRepository", "❌ Tarif dönüştürme hatası: ${e.message}")
                                    null
                                }
                            } ?: emptyList()

                        allRecipes.addAll(recipes)
                        Log.d("RecipeRepository", "✅ $category: ${recipes.size} tarif eklendi")
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
            Log.d("RecipeRepository", "🔍 Tarif getiriliyor: $id")

            // ✅ NEGATİF ID = BACKEND (Gemini tarifleri)
            if (id < 0) {
                Log.d("RecipeRepository", "🤖 Backend/Gemini tarifine gidiliyor: $id")

                val response = backendApi.getRecipeById(id)
                Log.d("RecipeRepository", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val recipe = response.body()!!.toRecipe()
                    Log.d("RecipeRepository", "✅ Backend tarif bulundu: ${recipe.name}")
                    Log.d("RecipeRepository", "   Ingredients: ${recipe.ingredients?.size ?: 0}")
                    return Resource.Success(recipe)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RecipeRepository", "❌ Backend error: $errorBody")
                    return Resource.Error("Backend tarif getirilemedi: ${response.code()}")
                }
            }
            // ✅ POZİTİF ID = MEALDB
            else {
                Log.d("RecipeRepository", "🍔 MealDB tarifine gidiliyor: $id")

                val response = mealDbApi.getMealById(id.toString())

                if (response.isSuccessful && response.body()?.meals != null) {
                    val meal = response.body()!!.meals?.firstOrNull()
                    if (meal != null) {
                        val recipe = meal.toRecipe()

                        // ✅ İsim çevirisi
                        val translatedName = try {
                            translationManager.translate(recipe.name)
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "⚠️ İsim çeviri hatası: ${e.message}")
                            recipe.name
                        }

                        // ✅ Talimat çevirisi
                        val translatedInstructions = try {
                            translationManager.translate(recipe.instructions)
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "⚠️ Talimat çeviri hatası: ${e.message}")
                            recipe.instructions
                        }

                        // ✅ MALZEMELERİ ÇEVİR
                        val translatedIngredients = try {
                            recipe.ingredients?.map { ingredient ->
                                try {
                                    translationManager.translate(ingredient)
                                } catch (e: Exception) {
                                    Log.e("RecipeRepository", "⚠️ Malzeme çeviri hatası: ${e.message}")
                                    ingredient // Hata olursa orijinal malzemeyi kullan
                                }
                            } ?: emptyList()
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "❌ Malzemeler çeviri hatası: ${e.message}")
                            recipe.ingredients ?: emptyList()
                        }

                        Resource.Success(
                            recipe.copy(
                                name = translatedName,
                                instructions = translatedInstructions,
                                ingredients = translatedIngredients  // ✅ ÇEVRİLMİŞ MALZEMELER
                            )
                        )
                    } else {
                        Resource.Error("MealDB'de tarif bulunamadı")
                    }
                } else {
                    Resource.Error("MealDB tarif getirilemedi")
                }
            }

        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ getRecipeById error: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Tarif yüklenemedi")
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

                                // ✅ İsmi çevir
                                val translatedName = try {
                                    translationManager.translate(recipe.name)
                                } catch (e: Exception) {
                                    Log.e("RecipeRepository", "⚠️ İsim çeviri hatası: ${e.message}")
                                    recipe.name
                                }

                                // ✅ Malzemeleri çevir
                                val translatedIngredients = try {
                                    recipe.ingredients?.map { ingredient ->
                                        try {
                                            translationManager.translate(ingredient)
                                        } catch (e: Exception) {
                                            ingredient
                                        }
                                    } ?: emptyList()
                                } catch (e: Exception) {
                                    recipe.ingredients ?: emptyList()
                                }

                                recipes.add(
                                    recipe.copy(
                                        name = translatedName,
                                        ingredients = translatedIngredients
                                    )
                                )
                                Log.d("RecipeRepository", "✅ Random tarif ${index + 1}: ${translatedName}")
                            } catch (e: Exception) {
                                Log.e("RecipeRepository", "❌ Random tarif dönüştürme hatası", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "❌ Random tarif ${index + 1} hatası: ${e.message}")
                }
            }

            if (recipes.isNotEmpty()) {
                Resource.Success(recipes)
            } else {
                Resource.Error("Random tarifler yüklenemedi")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

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