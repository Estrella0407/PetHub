package com.example.pethub.ui.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Pet
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = savedStateHandle.get<String>("petId")!!

    private val _uiState = MutableStateFlow<PetProfileUiState>(PetProfileUiState.Loading)
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    init {
        loadPetDetails()
    }

    private fun loadPetDetails() {
        // Move the entire try-catch block inside the launch
        viewModelScope.launch {
            _uiState.value = PetProfileUiState.Loading
            try {
                // Get current User ID
                val userId = authRepository.getCurrentUserId()

                if (userId != null) {
                    // Pass userId to the repository
                    val result = petRepository.getPetById(userId, petId)

                    if (result.isSuccess) {
                        val pet = result.getOrNull()
                        _uiState.value = if (pet != null) {
                            PetProfileUiState.Success(pet)
                        } else {
                            PetProfileUiState.Error("Pet not found.")
                        }
                    } else {
                        _uiState.value = PetProfileUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load pet details.")
                    }
                } else {
                    _uiState.value = PetProfileUiState.Error("User not logged in.")
                }

            } catch (e: Exception) {
                _uiState.value = PetProfileUiState.Error("An unexpected error occurred.")
            }
    }
}

    // TODO: Add functions to update pet details, handle image uploads, and remove pet
}

sealed class PetProfileUiState {
    data object Loading : PetProfileUiState()
    data class Success(val pet: Pet) : PetProfileUiState()
    data class Error(val message: String) : PetProfileUiState()
}
