package com.senaaksoy.recipeai.di

import com.senaaksoy.recipeai.data.local.dao.RecipeDao
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.repository.RecipeRepositoryImpl
import com.senaaksoy.recipeai.domain.repository.RecipeRepository
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
        dao: RecipeDao
    ): RecipeRepository {
        return RecipeRepositoryImpl(api, dao)
    }
}