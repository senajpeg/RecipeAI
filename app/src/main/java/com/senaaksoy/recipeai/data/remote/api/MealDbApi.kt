package com.senaaksoy.recipeai.data.remote.api

import com.senaaksoy.recipeai.data.remote.dto.MealDbResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {

    // Tarifleri isme göre ara
    @GET("search.php")
    suspend fun searchMeals(
        @Query("s") query: String
    ): Response<MealDbResponse>

    // ID'ye göre tarif getir
    @GET("lookup.php")
    suspend fun getMealById(
        @Query("i") id: String
    ): Response<MealDbResponse>

    // Random tarif getir (Günün Önerisi için)
    @GET("random.php")
    suspend fun getRandomMeal(): Response<MealDbResponse>

    // Kategoriye göre filtrele
    @GET("filter.php")
    suspend fun getMealsByCategory(
        @Query("c") category: String
    ): Response<MealDbResponse>

    // İlk harfe göre tarifleri listele
    @GET("search.php")
    suspend fun getMealsByFirstLetter(
        @Query("f") letter: String
    ): Response<MealDbResponse>
}