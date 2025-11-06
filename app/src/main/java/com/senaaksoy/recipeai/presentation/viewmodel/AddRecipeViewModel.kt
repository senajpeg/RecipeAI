package com.senaaksoy.recipeai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senaaksoy.recipeai.data.remote.Resource
import com.senaaksoy.recipeai.domain.model.Recipe
import com.senaaksoy.recipeai.domain.repository.RecipeRepository
import com.senaaksoy.recipeai.presentation.state.AddRecipeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddRecipeState())
    val state: StateFlow<AddRecipeState> = _state.asStateFlow()

    // Form alanlarını güncelle
    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun updateInstructions(instructions: String) {
        _state.value = _state.value.copy(instructions = instructions)
    }

    fun updateCookingTime(time: String) {
        _state.value = _state.value.copy(cookingTime = time)
    }

    fun updateDifficulty(difficulty: String) {
        _state.value = _state.value.copy(difficulty = difficulty)
    }

    fun updateImageUrl(url: String) {
        _state.value = _state.value.copy(imageUrl = url)
    }

    // Tarif kaydet
    fun saveRecipe() {
        val currentState = _state.value

        // Validasyon
        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(error = "Tarif adı boş olamaz")
            return
        }

        if (currentState.instructions.isBlank()) {
            _state.value = currentState.copy(error = "Tarif talimatları boş olamaz")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            val recipe = Recipe(
                id = 0, // Yeni tarif için 0
                name = currentState.name,
                description = currentState.description.ifBlank { null },
                instructions = currentState.instructions,
                cookingTime = currentState.cookingTime.toIntOrNull(),
                difficulty = currentState.difficulty,
                imageUrl = currentState.imageUrl.ifBlank { null }
            )

            when (val result = repository.createRecipe(recipe)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Loading state zaten set edildi
                }
            }
        }
    }

    // Formu sıfırla
    fun resetForm() {
        _state.value = AddRecipeState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}