package com.senaaksoy.recipeai.data.remote.api

import com.senaaksoy.recipeai.data.remote.dto.MealDbResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {

    @GET("lookup.php")
    suspend fun getMealById(
        @Query("i") id: String
    ): Response<MealDbResponse>

    @GET("random.php")
    suspend fun getRandomMeal(): Response<MealDbResponse>

    @GET("filter.php")
    suspend fun getMealsByCategory(
        @Query("c") category: String
    ): Response<MealDbResponse>


}