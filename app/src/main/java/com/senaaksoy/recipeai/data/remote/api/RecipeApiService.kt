package com.senaaksoy.recipeai.data.remote.api

import com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe
import com.senaaksoy.recipeai.data.remote.dto.GenerateRecipeRequest
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecipeApiService {

    @GET("recipes")
    suspend fun getAllRecipes(): Response<List<RecipeDto>>

    @GET("recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: Int): Response<RecipeDto>

    @POST("recipes")
    suspend fun createRecipe(@Body recipe: RecipeDto): Response<RecipeDto>

    @PUT("recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Int,
        @Body recipe: RecipeDto
    ): Response<RecipeDto>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): Response<Unit>

    @POST("gemini/generate")
    suspend fun generateRecipe(
        @Body request: GenerateRecipeRequest
    ): Response<AiGeneratedRecipe>
}