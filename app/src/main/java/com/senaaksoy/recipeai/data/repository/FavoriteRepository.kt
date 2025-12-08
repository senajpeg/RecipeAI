package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.remote.api.FavoriteApi
import com.senaaksoy.recipeai.data.remote.dto.AddFavoriteRequest
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val api: FavoriteApi,
    private val tokenManager: TokenManager
) {

    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites: StateFlow<List<Recipe>> = _favorites.asStateFlow()

    private val _favoriteStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val favoriteStates: StateFlow<Map<Int, Boolean>> = _favoriteStates.asStateFlow()

    private fun getAuthToken(): String {
        val token = tokenManager.getToken()
        Log.d("FavoriteRepo", "Token: ${token?.take(20)}...")
        return "Bearer $token"
    }

    suspend fun loadFavorites(): Resource<List<Recipe>> {
        return try {
            Log.d("FavoriteRepo", "Loading favorites...")
            val response = api.getFavorites(getAuthToken())

            Log.d("FavoriteRepo", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val recipes = response.body()!!.map { it.toRecipe() }
                _favorites.value = recipes

                val newStates = recipes.associate { it.id to true }
                _favoriteStates.value = _favoriteStates.value + newStates

                Log.d("FavoriteRepo", "✅ ${recipes.size} favori yüklendi")
                recipes.forEach {
                    Log.d("FavoriteRepo", "  - ${it.name} (${it.ingredients?.size ?: 0} malzeme)")
                }

                Resource.Success(recipes)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FavoriteRepo", "❌ Error: $errorBody")
                Resource.Error("Favoriler yüklenemedi: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "❌ Exception: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    suspend fun checkFavorite(recipeId: Int): Boolean {
        return try {
            Log.d("FavoriteRepo", "Checking favorite status for recipe: $recipeId")
            val response = api.isFavorite(recipeId, getAuthToken())

            if (response.isSuccessful) {
                val isFav = response.body() ?: false
                _favoriteStates.value = _favoriteStates.value + (recipeId to isFav)
                Log.d("FavoriteRepo", "Recipe $recipeId favorite status: $isFav")
                isFav
            } else {
                Log.e("FavoriteRepo", "Check favorite failed: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "Check favorite error: ${e.message}", e)
            false
        }
    }

    suspend fun toggleFavorite(recipe: Recipe): Resource<Unit> {
        return try {
            val currentlyFavorite = _favoriteStates.value[recipe.id] ?: false

            Log.d("FavoriteRepo", "=== TOGGLE FAVORITE ===")
            Log.d("FavoriteRepo", "Recipe: ${recipe.name}")
            Log.d("FavoriteRepo", "ID: ${recipe.id}")
            Log.d("FavoriteRepo", "Current state: $currentlyFavorite")
            Log.d("FavoriteRepo", "Ingredients: ${recipe.ingredients?.joinToString()}")

            val response = if (currentlyFavorite) {
                Log.d("FavoriteRepo", "Removing from favorites...")
                api.removeFavorite(recipe.id, getAuthToken())
            } else {
                Log.d("FavoriteRepo", "Adding to favorites...")

                // ✅ İNGREDIENTS DAHİL GÖNDER
                val request = AddFavoriteRequest(
                    id = recipe.id,
                    name = recipe.name,
                    description = recipe.description,
                    instructions = recipe.instructions,
                    cookingTime = recipe.cookingTime,
                    difficulty = recipe.difficulty,
                    imageUrl = recipe.imageUrl,
                    ingredients = recipe.ingredients  // ✅ EKLENDI
                )

                Log.d("FavoriteRepo", "Request payload:")
                Log.d("FavoriteRepo", "  - name: ${request.name}")
                Log.d("FavoriteRepo", "  - ingredients: ${request.ingredients?.size ?: 0} items")

                api.addFavorite(recipe.id, getAuthToken(), request)
            }

            Log.d("FavoriteRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val newState = !currentlyFavorite
                _favoriteStates.value = _favoriteStates.value + (recipe.id to newState)

                Log.d("FavoriteRepo", "✅ Toggle başarılı! Yeni durum: $newState")

                // Favori listesini güncelle
                loadFavorites()

                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FavoriteRepo", "❌ Toggle failed: $errorBody")
                Resource.Error("İşlem başarısız: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "❌ Toggle error: ${e.message}", e)
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }
}