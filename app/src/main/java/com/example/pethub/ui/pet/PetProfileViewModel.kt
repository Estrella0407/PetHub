package com.example.pethub.ui.pet

import android.net.Uri
import android.util.Log
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
import kotlin.jvm.optionals.getOrNull

@HiltViewModel
class PetProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String? = savedStateHandle.get<String>("petId")

    private val _uiState = MutableStateFlow<PetProfileUiState>(PetProfileUiState.Loading)
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    init {
        // Hardcoded check for "test-pet"
        if (petId == "test-pet") {
            val fakePet = Pet(
                petId = "test-pet",
                petName = "Lucky",
                type = "Dog",
                breed = "Poodle",
                sex = "Male",
                weight = 25,
                remarks = "Loves to play fetch and is a very good boy.",
                dateOfBirth = 1672531200000L, // Jan 1, 2023
                imageUrl = ""
            )
            _uiState.value = PetProfileUiState.Success(fakePet)
        } else if (petId == null) {
            _uiState.value = PetProfileUiState.Error("Pet ID is missing.")
            Log.e("PetProfileVM", "CRITICAL: petId is null. Navigation argument was not passed correctly.")
        } else {
            loadPetDetails(petId)
        }
    }

    private fun loadPetDetails(pId: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = PetProfileUiState.Error("User not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PetProfileUiState.Loading
            try {
                // *** FIX #1: Calling the correct repository function ***
                val result = petRepository.getPetById( pId)

                if (result.isSuccess) {
                    val pet = result.getOrNull()
                    _uiState.value = if (pet != null) {
                        PetProfileUiState.Success(pet = pet)
                    } else {
                        PetProfileUiState.Error("Pet not found.")
                    }
                } else {
                    _uiState.value = PetProfileUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load pet details."
                    )
                }
            } catch (e: Exception) {
                _uiState.value =
                    PetProfileUiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        val currentPetId = petId ?: return
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = PetProfileUiState.Error("User not logged in. Cannot upload image.")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    if (state is PetProfileUiState.Success) {
                        state.copy(isUploading = true)
                    } else state
                }

                val result = petRepository.uploadPetImage(userId, currentPetId, uri)

                if (result.isSuccess) {
                    val imageUrl = result.getOrThrow()
                    petRepository.updatePet(userId, currentPetId, mapOf("imageUrl" to imageUrl))
                    _uiState.update { state ->
                        if (state is PetProfileUiState.Success) {
                            state.copy(
                                pet = state.pet.copy(imageUrl = imageUrl),
                                isUploading = false
                            )
                        } else state
                    }
                } else {
                    val errorMsg = "Upload failed: ${result.exceptionOrNull()?.message}"
                    Log.e("PetProfileVM", errorMsg)
                    _uiState.update { state ->
                        if (state is PetProfileUiState.Success) {
                            state.copy(isUploading = false)
                        } else state
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "An unexpected error occurred during upload: ${e.message}"
                Log.e("PetProfileVM", errorMsg)
                _uiState.update { state ->
                    if (state is PetProfileUiState.Success) {
                        state.copy(isUploading = false)
                    } else state
                }
            }
        }
    }
} // <-- *** END OF PetProfileViewModel CLASS ***

// *** FIX #2: Moved the sealed class OUTSIDE of the ViewModel class ***
sealed class PetProfileUiState {
    data object Loading : PetProfileUiState()
    data class Success(
        val pet: Pet,
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f
    ) : PetProfileUiState()

    data class Error(val message: String) : PetProfileUiState()
}
