package com.senaaksoy.recipeai.data.remote.api

import com.senaaksoy.recipeai.data.remote.dto.AddFavoriteRequest
import com.senaaksoy.recipeai.data.remote.dto.MessageResponse
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import retrofit2.Response
import retrofit2.http.*

interface FavoriteApi {

    // ✅ BODY ile tarif verisini de gönder
    @POST("favorites/{recipeId}")
    suspend fun addFavorite(
        @Path("recipeId") recipeId: Int,
        @Header("Authorization") token: String,
        @Body recipe: AddFavoriteRequest
    ): Response<MessageResponse>

    @DELETE("favorites/{recipeId}")
    suspend fun removeFavorite(
        @Path("recipeId") recipeId: Int,
        @Header("Authorization") token: String
    ): Response<MessageResponse>

    @GET("favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): Response<List<RecipeDto>>

    @GET("favorites/check/{recipeId}")
    suspend fun isFavorite(
        @Path("recipeId") recipeId: Int,
        @Header("Authorization") token: String
    ): Response<Boolean>
}

