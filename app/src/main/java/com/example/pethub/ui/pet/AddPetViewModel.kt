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
        weight: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                // Handle error: user not logged in
                return@launch
            }

            val dateOfBirth = try {
                // We now return a Date object directly, not the time in milliseconds
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dob)
            } catch (e: Exception) {
                null
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
                onSuccess()
            } else {
                // Handle error
            }
        }
    }
}
