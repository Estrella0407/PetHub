package com.example.pethub.ui.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.local.database.entity.CustomerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // States for the input fields
    private val _petName = MutableStateFlow("")
    val petName: StateFlow<String> = _petName.asStateFlow()

    private val _petBreed = MutableStateFlow("")
    val petBreed: StateFlow<String> = _petBreed.asStateFlow()

    private val _petAge = MutableStateFlow("")
    val petAge: StateFlow<String> = _petAge.asStateFlow()

    private val _petGender = MutableStateFlow("")
    val petGender: StateFlow<String> = _petGender.asStateFlow()

    // Functions to update the state from the UI
    fun onNameChange(newName: String) {
        _petName.value = newName
    }

    fun onBreedChange(newBreed: String) {
        _petBreed.value = newBreed
    }

    fun onAgeChange(newAge: String) {
        _petAge.value = newAge
    }

    fun onGenderChange(newGender: String) {
        _petGender.value = newGender
    }

    // Existing code to get owner details
    private val _ownerDetails = MutableStateFlow<CustomerEntity?>(null)
    val ownerDetails: StateFlow<CustomerEntity?> = _ownerDetails

    init {
        getOwnerDetails()
    }

    private fun getOwnerDetails() {
        viewModelScope.launch {
            val result = authRepository.getCustomerDetails()

            if (result.isSuccess) {
                // The correct way to get the value from a kotlin.Result
                _ownerDetails.value = result.getOrNull()
            } else {
                _ownerDetails.value = null
            }
        }
    }

    fun onAddPetClicked() {
        viewModelScope.launch {
            // TODO: Implement the logic to save the pet to the repository
            // val pet = Pet(name = _petName.value, breed = _petBreed.value, ...)
            // petRepository.addPet(pet)
        }
    }
}
