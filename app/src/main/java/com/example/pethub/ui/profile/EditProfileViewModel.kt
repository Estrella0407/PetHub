package com.example.pethub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val houseNo: String = "",
    val streetName: String = "",
    val city: String = "",
    val postcode: String = "",
    val state: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            customerRepository.getCurrentCustomer().onSuccess { customer ->
                customer?.let {
                    val addressParts = it.custAddress.split(", ").map { part -> part.trim() }
                    _uiState.value = _uiState.value.copy(
                        name = it.custName,
                        phone = it.custPhone,
                        houseNo = addressParts.getOrNull(0) ?: "",
                        streetName = addressParts.getOrNull(1) ?: "",
                        city = addressParts.getOrNull(2) ?: "",
                        postcode = addressParts.getOrNull(3) ?: "",
                        state = addressParts.getOrNull(4) ?: "",
                        isLoading = false
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load profile data"
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun onHouseNoChange(houseNo: String) {
        _uiState.value = _uiState.value.copy(houseNo = houseNo)
    }

    fun onStreetNameChange(streetName: String) {
        _uiState.value = _uiState.value.copy(streetName = streetName)
    }

    fun onCityChange(city: String) {
        _uiState.value = _uiState.value.copy(city = city)
    }

    fun onPostcodeChange(postcode: String) {
        _uiState.value = _uiState.value.copy(postcode = postcode)
    }

    fun onStateChange(state: String) {
        _uiState.value = _uiState.value.copy(state = state)
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.name.isBlank() || state.phone.isBlank() || state.houseNo.isBlank() || 
            state.streetName.isBlank() || state.city.isBlank() || state.postcode.isBlank() || state.state.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "All fields are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val fullAddress = "${state.houseNo}, ${state.streetName}, ${state.city}, ${state.postcode}, ${state.state}"
            val result = customerRepository.updateCustomerDetails(
                name = state.name,
                phone = state.phone,
                address = fullAddress
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile"
                )
            }
        }
    }
}
