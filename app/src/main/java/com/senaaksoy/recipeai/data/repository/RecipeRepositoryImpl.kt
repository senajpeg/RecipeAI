package com.senaaksoy.recipeai.data.repository

import android.util.Log
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.MealDbApi
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.MealDbResponse
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import com.senaaksoy.recipeai.data.remote.dto.toEntity
import com.senaaksoy.recipeai.data.remote.dto.toRecipe
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.utills.NetworkUtils
import com.senaaksoy.recipeai.utills.Resource
import com.senaaksoy.recipeai.utills.TranslationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Response
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val backendApi: RecipeApiService,
    private val mealDbApi: MealDbApi,
    private val dao: RecipeDao,
    private val translationManager: TranslationManager,
    private val networkUtils: NetworkUtils
) : RecipeRepository {

    private suspend fun <T, R> safeRecipeRepoCall(
        operation: String,
        transform: (T) -> R,
        call: suspend () -> Response<T>
    ): Resource<R> {
        return try {
            Log.d("RecipeRepository", "Starting: $operation")

            val response = call()
            Log.d("RecipeRepository", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val rawData = response.body()!!
                val transformedData = transform(rawData)

                Log.d("RecipeRepository", "✅ $operation successful")
                Resource.Success(transformedData)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RecipeRepository", "❌ $operation failed: $errorBody")
                Resource.Error("$operation başarısız: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ $operation error: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    private suspend fun safeTranslate(text: String?, type: String = "text"): String {
        return try {
            if (text.isNullOrBlank()) return text ?: ""
            translationManager.translate(text)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "⚠️ $type çeviri hatası: ${e.message}")
            text ?: ""
        }
    }

    private suspend fun safeTranslateList(items: List<String>?, type: String = "items"): List<String> {
        return try {
            items?.map { item ->
                safeTranslate(item, type)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ $type çeviri listesi hatası: ${e.message}")
            items ?: emptyList()
        }
    }

    override fun getAllRecipesFromLocal(): Flow<List<Recipe>> {
        return dao.getAllRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }

    override suspend fun syncRecipesFromApi(): Resource<List<Recipe>> {
        if (!networkUtils.isNetworkAvailable()) {
            Log.d("RecipeRepository", "🔴 İnternet yok, Room'dan yükleniyor...")

            val recipeList = dao.getAllRecipes()
                .map { entities -> entities.map { it.toRecipe() } }
                .first()

            return if (recipeList.isNotEmpty()) {
                Resource.Success(recipeList)
            } else {
                Resource.Error("İnternet bağlantısı yok ve yerel veri bulunamadı")
            }
        }

        return try {
            Log.d("RecipeRepository", "🔵 İnternet var, MealDB'den tarifler çekiliyor...")

            val categories = listOf("Chicken", "Beef", "Pasta", "Seafood", "Dessert")
            val allRecipes = mutableListOf<Recipe>()

            for (category in categories) {
                val result = safeRecipeRepoCall(
                    operation = "Fetch category: $category",
                    transform = { response: MealDbResponse ->
                        response.meals?.take(3) ?: emptyList()
                    }
                ) {
                    mealDbApi.getMealsByCategory(category)
                }

                if (result is Resource.Success) {
                    val recipes = result.data?.mapNotNull { mealDto ->
                        try {

                            val quickRecipe = Recipe(
                                id = mealDto.idMeal.toIntOrNull() ?: mealDto.idMeal.hashCode(),
                                name = mealDto.name,
                                description = mealDto.category ?: "Lezzetli bir tarif",
                                instructions = "", // ❗ Boş, detay sayfasında yüklenecek
                                cookingTime = null,
                                difficulty = "Orta",
                                imageUrl = mealDto.imageUrl,
                                createdAt = System.currentTimeMillis(),
                                ingredients = emptyList() // ❗ Boş, detay sayfasında yüklenecek
                            )

                            // İsmi çevir (sadece gösterim için)
                            val translatedName = safeTranslate(quickRecipe.name, "Name")
                            quickRecipe.copy(name = translatedName)

                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "❌ Tarif dönüştürme hatası: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                    allRecipes.addAll(recipes)
                    Log.d("RecipeRepository", "✅ $category: ${recipes.size} tarif eklendi")
                }
            }

            if (allRecipes.isNotEmpty()) {

                val entities = allRecipes.map { it.toEntity(isFavorite = false) }
                dao.insertRecipes(entities)
                Log.d("RecipeRepository", "✅ ${allRecipes.size} tarif Room'a kaydedildi")

                Resource.Success(allRecipes)
            } else {
                Resource.Error("Tarifler yüklenemedi")
            }

        } catch (e: Exception) {
            Log.e("RecipeRepository", "❌ FATAL: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Bağlantı hatası")
        }
    }

    override suspend fun getRecipeById(id: Int): Resource<Recipe> {

        val localRecipe = dao.getRecipeById(id)


        if (localRecipe != null &&
            localRecipe.instructions.isNotBlank() &&
            localRecipe.ingredients.isNotEmpty()) {
            Log.d("RecipeRepository", "✅ Tarif Room'da EKSIKSIZ bulundu: ${localRecipe.name}")
            return Resource.Success(localRecipe.toRecipe())
        }


        if (localRecipe != null &&
            (localRecipe.instructions.isBlank() || localRecipe.ingredients.isEmpty())) {
            Log.d("RecipeRepository", "⚠️ Tarif Room'da eksik, API'den detay yükleniyor: ${localRecipe.name}")
        }

        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("İnternet bağlantısı yok ve tarif detayları eksik")
        }

        return if (id < 0 || id > 50000000) {

            val result = safeRecipeRepoCall(
                operation = "🤖 Fetch backend recipe: $id",
                transform = { dto: RecipeDto -> dto.toRecipe() }
            ) {
                backendApi.getRecipeById(id)
            }

            if (result is Resource.Success && result.data != null) {
                try {
                    val recipe = result.data
                    val translatedRecipe = recipe.copy(
                        name = safeTranslate(recipe.name, "Name"),
                        description = safeTranslate(recipe.description, "Description"),
                        instructions = safeTranslate(recipe.instructions, "Instructions"),
                        ingredients = safeTranslateList(recipe.ingredients, "Ingredients")
                    )

                    dao.insertRecipe(translatedRecipe.toEntity(isFavorite = false))
                    Log.d("RecipeRepository", "✅ Backend tarif çevrilip Room'a kaydedildi: ${translatedRecipe.name}")

                    Resource.Success(translatedRecipe)
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Translation failed, returning original", e)
                    dao.insertRecipe(result.data.toEntity(isFavorite = false))
                    result
                }
            } else {
                result
            }
        } else {

            val fetchResult = safeRecipeRepoCall(
                operation = "🍔 Fetch MealDB recipe DETAIL: $id",
                transform = { response: MealDbResponse ->
                    val meal = response.meals?.firstOrNull()
                        ?: throw Exception("MealDB'de tarif bulunamadı")
                    meal.toRecipe()
                }
            ) {
                mealDbApi.getMealById(id.toString())
            }

            when (fetchResult) {
                is Resource.Success -> {
                    val recipe = fetchResult.data ?: return Resource.Error("Tarif bulunamadı")


                    if (recipe.instructions.isBlank()) {
                        Log.e("RecipeRepository", "⚠️ MealDB'den instructions BOŞ geldi! Recipe ID: ${recipe.id}")
                    }

                    try {

                        val translatedRecipe = recipe.copy(
                            name = safeTranslate(recipe.name, "Name"),
                            description = safeTranslate(
                                recipe.description.takeIf { !it.isNullOrBlank() }
                                    ?: "Lezzetli bir ${recipe.name} tarifi",
                                "Description"
                            ),
                            instructions = safeTranslate(
                                recipe.instructions.takeIf { !it.isNullOrBlank() }
                                    ?: "Yapılış bilgisi mevcut değil",
                                "Instructions"
                            ),
                            ingredients = safeTranslateList(recipe.ingredients, "Ingredients")
                        )


                        dao.insertRecipe(translatedRecipe.toEntity(isFavorite = false))
                        Log.d("RecipeRepository", "✅ MealDB tarif detayı çevrilip Room'a GÜNCELLENDİ: ${translatedRecipe.name}")

                        Resource.Success(translatedRecipe)
                    } catch (e: Exception) {
                        Log.e("RecipeRepository", "Translation failed, returning original", e)
                        dao.insertRecipe(recipe.toEntity(isFavorite = false))
                        Resource.Success(recipe)
                    }
                }
                is Resource.Error -> fetchResult
                is Resource.Loading -> fetchResult
            }
        }
    }

    suspend fun getRandomRecipes(count: Int = 3): Resource<List<Recipe>> {
        if (!networkUtils.isNetworkAvailable()) {
            Log.d("RecipeRepository", "🔴 İnternet yok, Room'dan rastgele tarifler yükleniyor...")

            val recipeList = dao.getAllRecipes()
                .map { entities -> entities.map { it.toRecipe() }.shuffled().take(count) }
                .first()

            return if (recipeList.isNotEmpty()) {
                Resource.Success(recipeList)
            } else {
                Resource.Error("İnternet bağlantısı yok ve yerel veri bulunamadı")
            }
        }

        return try {
            Log.d("RecipeRepository", "🎲 $count adet random tarif çekiliyor...")
            val recipes = mutableListOf<Recipe>()

            repeat(count) { index ->
                val fetchResult = safeRecipeRepoCall(
                    operation = "Fetch random recipe ${index + 1}",
                    transform = { response: MealDbResponse ->
                        val mealDto = response.meals?.firstOrNull()
                            ?: throw Exception("Random tarif bulunamadı")
                        mealDto.toRecipe()
                    }
                ) {
                    mealDbApi.getRandomMeal()
                }

                if (fetchResult is Resource.Success) {
                    val recipe = fetchResult.data
                    if (recipe != null) {
                        // ⚠️ Instructions boş geldi mi kontrol et
                        if (recipe.instructions.isBlank()) {
                            Log.e("RecipeRepository", "⚠️ Random MealDB'den instructions BOŞ geldi! Recipe: ${recipe.name}")
                        }

                        try {
                            // ✅ Tüm alanları çevir (instructions dahil!)
                            val translatedRecipe = recipe.copy(
                                name = safeTranslate(recipe.name, "Name"),
                                description = safeTranslate(
                                    recipe.description.takeIf { !it.isNullOrBlank() }
                                        ?: "Lezzetli bir ${recipe.name} tarifi",
                                    "Description"
                                ),
                                instructions = safeTranslate(
                                    recipe.instructions.takeIf { !it.isNullOrBlank() }
                                        ?: "Yapılış bilgisi mevcut değil",
                                    "Instructions"
                                ),
                                ingredients = safeTranslateList(recipe.ingredients, "Ingredients")
                            )

                            dao.insertRecipe(translatedRecipe.toEntity(isFavorite = false))

                            recipes.add(translatedRecipe)
                            Log.d("RecipeRepository", "✅ Random tarif ${index + 1}: ${translatedRecipe.name}")
                        } catch (e: Exception) {
                            Log.e("RecipeRepository", "Translation failed, using original", e)
                            dao.insertRecipe(recipe.toEntity(isFavorite = false))
                            recipes.add(recipe)
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
            Log.e("RecipeRepository", "❌ Random recipes error: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Hata oluştu")
        }
    }

    override suspend fun createRecipe(recipe: Recipe): Resource<Recipe> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("Tarif eklemek için internet bağlantısı gerekli")
        }
        return Resource.Error("Henüz desteklenmiyor")
    }
}