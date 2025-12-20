package com.example.pethub.ui.pet

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Pet
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.PetRepository
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.model.Service
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
    private val appointmentRepository: AppointmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = savedStateHandle.get<String>("petId")!!

    private val _uiState = MutableStateFlow<PetProfileUiState>(PetProfileUiState.Loading)
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    init {
        loadPetDetails()
    }

    private fun loadPetDetails() {
        viewModelScope.launch {
            _uiState.value = PetProfileUiState.Loading
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = PetProfileUiState.Error("User not logged in.")
                    return@launch
                }

                val result = petRepository.getPetById(userId, petId)
                val pet = result.getOrNull()

                if (pet != null) {
                    // If pet is found, set the success state first
                    _uiState.value = PetProfileUiState.Success(pet = pet)

                    // Then, fetch recommendations and update the state
                    val recommendations = appointmentRepository.getRecommendedServicesForPet(
                        petType = pet.type,
                        petBreed = pet.breed
                    )
                    _uiState.update {
                        (it as PetProfileUiState.Success).copy(recommendedServices = recommendations)
                    }
                } else {
                    // If pet is not found, set the error state
                    _uiState.value = PetProfileUiState.Error("Pet not found.")
                }
            } catch (e: Exception) {
                _uiState.value = PetProfileUiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is PetProfileUiState.Success) {
                val userId = authRepository.getCurrentUserId() ?: return@launch

                _uiState.update { currentState.copy(isUploading = true) }

                try {
                    val result = petRepository.uploadPetImage(userId, petId, uri)
                    val newImageUrl = result.getOrThrow()

                    val updates = mapOf("imageUrl" to newImageUrl)
                    petRepository.updatePet(userId, petId, updates)

                    val updatedPet = currentState.pet.copy(imageUrl = newImageUrl)
                    _uiState.update {
                        currentState.copy(pet = updatedPet, isUploading = false)
                    }
                } catch (e: Exception) {
                    // On failure, revert to the previous state without the loading indicator
                    _uiState.value = currentState.copy(isUploading = false)
                    // Optionally, you can set an error message in the state to show in a toast/snackbar
                }
            }
        }
    }
    fun onPetDataChanged(updatedPet: Pet) {
        val currentState = _uiState.value
        if (currentState is PetProfileUiState.Success) {
            _uiState.value = currentState.copy(pet = updatedPet)
        }
    }

    fun savePetDetails() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is PetProfileUiState.Success) {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                val pet = currentState.pet

                _uiState.update { currentState.copy(isUploading = true) }

                try {
                    val updates = mapOf(
                        "petName" to pet.petName,
                        "sex" to pet.sex,
                        "breed" to pet.breed,
                        "weight" to pet.weight,
                        "remarks" to pet.remarks,
                        // Not updating DOB/Age via text to avoid parsing errors for now
                    )
                    petRepository.updatePet(userId, pet.petId, updates)

                    _uiState.update {
                        currentState.copy(isUploading = false)
                    }
                    // Optionally show a success message? For now just stop loading.
                } catch (e: Exception) {
                    _uiState.update {
                        PetProfileUiState.Error(e.message ?: "Failed to save changes")
                    }
                }
            }
        }
    }
}

sealed class PetProfileUiState {
    data object Loading : PetProfileUiState()

    data class Success(
        val pet: Pet,
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f,
        val recommendedServices: List<Service> = emptyList()
    ) : PetProfileUiState()

    data class Error(val message: String) : PetProfileUiState()
}
