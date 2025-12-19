package com.example.pethub.ui.pet

import android.net.Uri
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
import kotlinx.coroutines.flow.update
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
                        _uiState.value = PetProfileUiState.Error(
                            result.exceptionOrNull()?.message ?: "Failed to load pet details."
                        )
                    }
                } else {
                    _uiState.value = PetProfileUiState.Error("User not logged in.")
                }

            } catch (e: Exception) {
                _uiState.value = PetProfileUiState.Error("An unexpected error occurred.")
            }
        }
    }


    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is PetProfileUiState.Success) {
                val userId = authRepository.getCurrentUserId() ?: return@launch

                try {
                    // Set state to uploading
                    _uiState.update {
                        currentState.copy(isUploading = true, uploadProgress = 0f)
                    }

                    // Upload to Cloudinary (via Repository)
                    // Note: You might need to check if PetRepository has 'uploadPetImage' taking a progress callback
                    // If not, standard upload is fine.
                    val result = petRepository.uploadPetImage(userId, petId, uri)

                    if (result.isSuccess) {
                        val newImageUrl = result.getOrNull()!!

                        // Update Firestore with new URL
                        // Create a map of updates
                        val updates = mapOf("imageUrl" to newImageUrl)
                        petRepository.updatePet(userId, petId, updates)

                        // Update UI State with new image and stop loading
                        val updatedPet = currentState.pet.copy(imageUrl = newImageUrl)
                        _uiState.update {
                            currentState.copy(pet = updatedPet, isUploading = false)
                        }
                    } else {
                        throw Exception(result.exceptionOrNull())
                    }

                } catch (e: Exception) {
                    // Handle failure
                    _uiState.update {
                        PetProfileUiState.Error("Upload failed: ${e.message}")
                    }
                    // Reload data to reset state
                    loadPetDetails()
                }
            }
        }
    }
}

sealed class PetProfileUiState {
    data object Loading : PetProfileUiState()

    // 👇 UPDATED: Added upload fields
    data class Success(
        val pet: Pet,
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f
    ) : PetProfileUiState()

    data class Error(val message: String) : PetProfileUiState()
}
