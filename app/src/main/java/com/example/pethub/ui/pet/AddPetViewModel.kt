package com.example.pethub.ui.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Customer
import com.example.pethub.data.model.Pet
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.CustomerRepository
import com.example.pethub.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _ownerDetails = MutableStateFlow<Customer?>(null)
    val ownerDetails: StateFlow<Customer?> = _ownerDetails.asStateFlow()

    // Event Channel
    private val _uiEvent = kotlinx.coroutines.channels.Channel<AddPetEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadOwnerDetails()
    }

    private fun loadOwnerDetails() {
        viewModelScope.launch {
            val result = customerRepository.getCurrentCustomer()
            result.onSuccess {
                _ownerDetails.value = it
            }
        }
    }

    fun savePet(
        petName: String,
        type: String,
        breed: String,
        remarks: String,
        dob: String,
        sex: String,
        weight: String
    ) {
        viewModelScope.launch {
            // Validation with specific feedback and debug info
            val missingFields = mutableListOf<String>()
            if (petName.isBlank()) missingFields.add("Name")
            if (type.isBlank()) missingFields.add("Type")
            if (breed.isBlank()) missingFields.add("Breed")
            if (dob.isBlank()) missingFields.add("Date of Birth")
            if (sex == "Select") missingFields.add("Gender")

            if (missingFields.isNotEmpty()) {
                val debugInfo = "Name='$petName', Type='$type', Breed='$breed', DOB='$dob', Sex='$sex'"
                _uiEvent.send(AddPetEvent.ShowMessage("Missing: ${missingFields.joinToString(", ")}\n[Debug: $debugInfo]"))
                return@launch
            }

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiEvent.send(AddPetEvent.ShowMessage("User not logged in"))
                return@launch
            }

            val dateOfBirth = try {
                // We now return a Date object directly, not the time in milliseconds
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dob)
            } catch (e: Exception) {
                _uiEvent.send(AddPetEvent.ShowMessage("Invalid Date format"))
                return@launch
            }

            val newPet = Pet(
                petName = petName,
                type = type,
                breed = breed,
                remarks = remarks,
                dateOfBirth = dateOfBirth, // Pass Date? instead of Long?
                sex = sex,
                weight = weight.toDoubleOrNull(),
                custId = userId
            )

            val result = petRepository.addPet(userId, newPet)
            if (result.isSuccess) {
                // MODIFIED: Get the new ID from the result
                val newPetId = result.getOrNull()
                if (newPetId != null) {
                    _uiEvent.send(AddPetEvent.PetAdded(newPetId))
                } else {
                     _uiEvent.send(AddPetEvent.ShowMessage("Failed to get new pet ID"))
                }
            } else {
                _uiEvent.send(AddPetEvent.ShowMessage("Failed to add pet: ${result.exceptionOrNull()?.message}"))
            }
        }
    }
}

sealed class AddPetEvent {
    data class ShowMessage(val message: String) : AddPetEvent()
    data class PetAdded(val petId: String) : AddPetEvent()
}
