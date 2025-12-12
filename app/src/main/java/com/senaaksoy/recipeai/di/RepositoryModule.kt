package com.senaaksoy.recipeai.di

import com.google.gson.Gson
import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.FavoriteApi
import com.senaaksoy.recipeai.data.remote.api.MealDbApi
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.repository.FavoriteRepository
import com.senaaksoy.recipeai.data.repository.GeminiRepository
import com.senaaksoy.recipeai.data.repository.RecipeRepository
import com.senaaksoy.recipeai.data.repository.RecipeRepositoryImpl
import com.senaaksoy.recipeai.utills.NetworkUtils
import com.senaaksoy.recipeai.utills.TokenManager
import com.senaaksoy.recipeai.utills.TranslationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRecipeRepository(
        api: RecipeApiService,
        mealDbApi: MealDbApi,
        dao: RecipeDao,
        translationManager: TranslationManager,
        networkUtils: NetworkUtils
    ): RecipeRepository {
        return RecipeRepositoryImpl(api, mealDbApi, dao, translationManager, networkUtils)
    }


    @Provides
    @Singleton
    fun provideGeminiRepository(
        api: RecipeApiService,
        gson: Gson
    ): GeminiRepository {
        return GeminiRepository(api, gson)
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        api: FavoriteApi,
        tokenManager: TokenManager,
        dao: RecipeDao,
        networkUtils: NetworkUtils
    ): FavoriteRepository {
        return FavoriteRepository(api, tokenManager, dao, networkUtils)
    }

}