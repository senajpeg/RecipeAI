package com.senaaksoy.recipeai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import com.senaaksoy.recipeai.data.repository.GeminiRepository
import com.senaaksoy.recipeai.presentation.state.AddRecipeState
import com.senaaksoy.recipeai.utills.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val recipeApi: RecipeApiService
) : ViewModel() {

    private val _state = MutableStateFlow(AddRecipeState())
    val state: StateFlow<AddRecipeState> = _state.asStateFlow()

    fun addIngredient(ingredient: String) {
        val currentIngredients = _state.value.ingredients.toMutableList()
        if (!currentIngredients.contains(ingredient)) {
            currentIngredients.add(ingredient)
            _state.value = _state.value.copy(ingredients = currentIngredients)
        }
    }

    fun removeIngredient(ingredient: String) {
        val currentIngredients = _state.value.ingredients.toMutableList()
        currentIngredients.remove(ingredient)
        _state.value = _state.value.copy(ingredients = currentIngredients)
    }

    fun generateRecipe() {
        val ingredients = _state.value.ingredients

        if (ingredients.isEmpty()) {
            _state.value = _state.value.copy(error = "Lütfen en az bir malzeme ekleyin")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = geminiRepository.generateRecipe(ingredients)) {
                is Resource.Success -> {
                    val aiRecipe = result.data

                    if (aiRecipe != null) {

                        saveRecipeToBackend(aiRecipe)
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Tarif oluşturulamadı"
                        )
                    }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Loading zaten set edildi
                }
            }
        }
    }

    private suspend fun saveRecipeToBackend(aiRecipe: com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe) {
        try {
            Log.d("AddRecipeVM", "🔵 Tarif backend'e kaydediliyor: ${aiRecipe.name}")

            val recipeId = -System.currentTimeMillis().toInt()

            val recipeDto = RecipeDto(
                id = recipeId,
                name = aiRecipe.name,
                description = aiRecipe.description,
                instructions = aiRecipe.instructions,
                cookingTime = aiRecipe.cookingTime,
                difficulty = aiRecipe.difficulty,
                imageUrl = null,
                ingredients = aiRecipe.ingredients,
                createdAt = null
            )

            val response = recipeApi.createRecipe(recipeDto)

            if (response.isSuccessful && response.body() != null) {
                val savedRecipe = response.body()!!
                Log.d("AddRecipeVM", "✅ Tarif backend'e kaydedildi! ID: ${savedRecipe.id}")


                _state.value = _state.value.copy(
                    isLoading = false,
                    generatedRecipe = aiRecipe,
                    savedRecipeId = savedRecipe.id,
                    error = null
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AddRecipeVM", "❌ Backend kayıt hatası: $errorBody")


                _state.value = _state.value.copy(
                    isLoading = false,
                    generatedRecipe = aiRecipe,
                    error = "Tarif oluşturuldu ama kaydedilemedi"
                )
            }

        } catch (e: Exception) {
            Log.e("AddRecipeVM", "❌ Backend kayıt exception: ${e.message}", e)


            _state.value = _state.value.copy(
                isLoading = false,
                generatedRecipe = aiRecipe,
                error = "Tarif oluşturuldu ama kaydedilemedi: ${e.message}"
            )
        }
    }

}