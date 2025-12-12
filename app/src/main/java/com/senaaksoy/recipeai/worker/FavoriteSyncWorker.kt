package com.senaaksoy.recipeai.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.FavoriteApi
import com.senaaksoy.recipeai.data.remote.dto.AddFavoriteRequest
import com.senaaksoy.recipeai.utills.TokenManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FavoriteSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: RecipeDao,
    private val api: FavoriteApi,
    private val tokenManager: TokenManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = tokenManager.getToken() ?: return Result.failure()
        val authHeader = "Bearer $token"

        val unsyncedRecipes = dao.getUnsyncedRecipes()

        if (unsyncedRecipes.isEmpty()) return Result.success()

        Log.d("FavoriteSyncWorker", "🔄 Sync başladı: ${unsyncedRecipes.size} tarif işleniyor...")

        return try {
            unsyncedRecipes.forEach { recipe ->
                if (recipe.isFavorite) {
                    // Ekleme Senaryosu
                    val request = AddFavoriteRequest(
                        id = recipe.id,
                        name = recipe.name,
                        description = recipe.description,
                        instructions = recipe.instructions,
                        cookingTime = recipe.cookingTime,
                        difficulty = recipe.difficulty,
                        imageUrl = recipe.imageUrl,
                        ingredients = recipe.ingredients
                    )
                    val response = api.addFavorite(recipe.id, authHeader, request)
                    if (response.isSuccessful) {
                        dao.markAsSynced(recipe.id)
                        Log.d("FavoriteSyncWorker", "✅ Eklendi ve senkronize oldu: ${recipe.name}")
                    }
                } else {
                    // Silme Senaryosu
                    val response = api.removeFavorite(recipe.id, authHeader)
                    if (response.isSuccessful || response.code() == 404) {
                        dao.markAsSynced(recipe.id)
                        Log.d("FavoriteSyncWorker", "✅ Silindi ve senkronize oldu: ${recipe.name}")
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("FavoriteSyncWorker", "❌ Sync hatası", e)
            Result.retry()
        }
    }
}