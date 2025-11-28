package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.remote.api.MealDbApi
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.domain.repository.RecipeRepository
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
            Log.d("RecipeRepository", "Fetching recipes from MealDB...")

            val categories = listOf("Chicken", "Beef", "Pasta", "Seafood", "Dessert")
            val allRecipes = mutableListOf<Recipe>()

            categories.forEach { category ->
                try {
                    val response = mealDbApi.getMealsByCategory(category)
                    if (response.isSuccessful && response.body()?.meals != null) {
                        val recipes = response.body()!!.meals
                            ?.take(3)
                            ?.mapNotNull { mealDto ->
                                try {
                                    val recipe = mealDto.toRecipe()
                                    // Tarif adını Türkçe'ye çevir
                                    val translatedName = translationManager.translate(recipe.name)
                                    recipe.copy(name = translatedName)
                                } catch (e: Exception) {
                                    Log.e("RecipeRepository", "Error converting meal: ${e.message}")
                                    null
                                }
                            } ?: emptyList()

                        allRecipes.addAll(recipes)
                        Log.d("RecipeRepository", "Added ${recipes.size} recipes from $category")
                    }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error fetching $category: ${e.message}")
                }
            }

            if (allRecipes.isNotEmpty()) {
                Log.d("RecipeRepository", "Total recipes fetched: ${allRecipes.size}")
                Resource.Success(allRecipes)
            } else {
                Resource.Error("Tarifler yüklenemedi")
            }

        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error in syncRecipesFromApi", e)
            Resource.Error(e.localizedMessage ?: "Bağlantı hatası")
        }
    }

    // ID'ye göre tarif getir ve çevir
    override suspend fun getRecipeById(id: Int): Resource<Recipe> {
        return try {
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

    // Random tarifleri getir ve çevir
    suspend fun getRandomRecipes(count: Int = 3): Resource<List<Recipe>> {
        return try {
            val recipes = mutableListOf<Recipe>()

            repeat(count) {
                val response = mealDbApi.getRandomMeal()
                if (response.isSuccessful && response.body()?.meals != null) {
                    response.body()!!.meals?.firstOrNull()?.let { mealDto ->
                        try {
                            val recipe = mealDto.toRecipe()
                            val translatedName = translationManager.translate(recipe.name)
                            recipes.add(recipe.copy(name = translatedName))
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "Error converting random meal", e)
                        }
                    }
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