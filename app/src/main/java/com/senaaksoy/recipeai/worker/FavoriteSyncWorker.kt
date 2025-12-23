package com.senaaksoy.recipeai.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.FavoriteApi
import com.senaaksoy.recipeai.data.remote.dto.toAddFavoriteRequest
import com.senaaksoy.recipeai.utills.TokenManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FavoriteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: RecipeDao,
    private val api: FavoriteApi,
    private val tokenManager: TokenManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d("FavoriteSyncWorker", "🔄 Sync başlatılıyor...")

        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            Log.e("FavoriteSyncWorker", "❌ Token bulunamadı, retry")
            return Result.retry()
        }

        val authHeader = "Bearer $token"
        val unsyncedRecipes = dao.getUnsyncedRecipes()

        Log.d("FavoriteSyncWorker", "📋 Sync bekleyen tarif sayısı: ${unsyncedRecipes.size}")

        if (unsyncedRecipes.isEmpty()) {
            Log.d("FavoriteSyncWorker", "✅ Sync edilecek tarif yok")
            return Result.success()
        }

        var successCount = 0
        var failCount = 0

        unsyncedRecipes.forEach { recipe ->
            try {
                if (recipe.isFavorite) {
                    Log.d("FavoriteSyncWorker", "➕ Favoriye ekleniyor: ${recipe.name} (ID: ${recipe.id})")

                    val response = api.addFavorite(
                        recipe.id,
                        authHeader,
                        recipe.toAddFavoriteRequest()
                    )

                    if (response.isSuccessful) {
                        Log.d("FavoriteSyncWorker", "✅ Backend'e eklendi: ${recipe.name}")
                        dao.markAsSynced(recipe.id)
                        successCount++
                    } else if (response.code() == 409) {
                        Log.d("FavoriteSyncWorker", "⚠️ Zaten var (409): ${recipe.name}")
                        dao.markAsSynced(recipe.id)
                        successCount++
                    } else {
                        Log.e("FavoriteSyncWorker", "❌ Backend hatası (${response.code()}): ${recipe.name}")
                        failCount++
                    }
                } else {
                    Log.d("FavoriteSyncWorker", "➖ Favoriden siliniyor: ${recipe.name} (ID: ${recipe.id})")

                    val response = api.removeFavorite(recipe.id, authHeader)

                    if (response.isSuccessful) {
                        Log.d("FavoriteSyncWorker", "✅ Backend'den silindi: ${recipe.name}")
                        dao.markAsSynced(recipe.id)
                        successCount++
                    } else if (response.code() == 404) {
                        Log.d("FavoriteSyncWorker", "⚠️ Zaten yok (404): ${recipe.name}")
                        dao.markAsSynced(recipe.id)
                        successCount++
                    } else {
                        Log.e("FavoriteSyncWorker", "❌ Backend hatası (${response.code()}): ${recipe.name}")
                        failCount++
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoriteSyncWorker", "❌ Exception: ${recipe.name}", e)
                failCount++
            }
        }

        Log.d("FavoriteSyncWorker", "📊 Sync sonucu - Başarılı: $successCount, Başarısız: $failCount")

        return if (failCount == 0) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}