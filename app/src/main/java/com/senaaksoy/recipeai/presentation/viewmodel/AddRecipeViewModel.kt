package com.senaaksoy.recipeai.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.data.remote.api.RecipeApiService
import com.senaaksoy.recipeai.data.remote.dto.RecipeDto
import com.senaaksoy.recipeai.data.repository.GeminiRepository
import com.senaaksoy.recipeai.presentation.state.AddRecipeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val recipeApi: RecipeApiService  // ✅ BACKEND API EKLENDI
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
                        // ✅ BACKEND'E KAYDET
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

    // ✅ YENİ: Backend'e tarif kaydetme
    private suspend fun saveRecipeToBackend(aiRecipe: com.senaaksoy.recipeai.data.remote.dto.AiGeneratedRecipe) {
        try {
            Log.d("AddRecipeVM", "🔵 Tarif backend'e kaydediliyor: ${aiRecipe.name}")

            // ✅ Negatif ID kullan (Gemini tarifleri için)
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

                // ✅ State'i güncelle - kaydedilen recipe'yi kullan
                _state.value = _state.value.copy(
                    isLoading = false,
                    generatedRecipe = aiRecipe,
                    savedRecipeId = savedRecipe.id,  // Kaydedilen ID'yi sakla
                    error = null
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AddRecipeVM", "❌ Backend kayıt hatası: $errorBody")

                // Hata olsa bile AI recipe'yi göster ama uyarı ver
                _state.value = _state.value.copy(
                    isLoading = false,
                    generatedRecipe = aiRecipe,
                    error = "Tarif oluşturuldu ama kaydedilemedi"
                )
            }

        } catch (e: Exception) {
            Log.e("AddRecipeVM", "❌ Backend kayıt exception: ${e.message}", e)

            // Exception olsa bile AI recipe'yi göster
            _state.value = _state.value.copy(
                isLoading = false,
                generatedRecipe = aiRecipe,
                error = "Tarif oluşturuldu ama kaydedilemedi: ${e.message}"
            )
        }
    }

    fun resetState() {
        _state.value = AddRecipeState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}