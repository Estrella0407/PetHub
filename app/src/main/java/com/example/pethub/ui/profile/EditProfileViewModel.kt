/*
package com.example.pethub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Customer
import com.example.pethub.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadInitialCustomerData()
    }

    private fun loadInitialCustomerData() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            customerRepository.listenToCurrentCustomer().collect { customer ->
                if (customer != null) {
                    _uiState.value = EditProfileUiState.Success(
                        customer = customer,
                        // Initialize state variables with data from Firestore
                        name = customer.custName ?: "",
                        phone = customer.custPhone ?: "",
                        unitNo = customer.custAddress?.unitNo ?: "",
                        street = customer.custAddress?.street ?: "",
                        city = customer.custAddress?.city ?: "",
                        postcode = customer.custAddress?.postcode ?: "",
                        state = customer.custAddress?.state ?: "Select State"
                    )
                } else {
                    _uiState.value = EditProfileUiState.Error("User data not found.")
                }
            }
        }
    }

    fun updateName(name: String) {
        if (_uiState.value is EditProfileUiState.Success) {
            _uiState.value = (_uiState.value as EditProfileUiState.Success).copy(name = name)
        }
    }

    fun updatePhone(phone: String) {
        if (_uiState.value is EditProfileUiState.Success) {
            _uiState.value = (_uiState.value as EditProfileUiState.Success).copy(phone = phone)
        }
    }

    // Add similar update functions for address fields...

    // TODO: Implement the saveCustomerDetails function
    fun saveCustomerDetails() {
        // Here you would collect the data from the Success state and
        // call the customerRepository to update the document in Firestore.
    }
}

sealed class EditProfileUiState {
    data object Loading : EditProfileUiState()

    data class Success(
        val customer: Customer?,
        val name: String,
        val phone: String,
        val unitNo: String,
        val street: String,
        val city: String,
        val postcode: String,
        val state: String,
        val isSaving: Boolean = false
    ) : EditProfileUiState()

    data class Error(val message: String) : EditProfileUiState()
}
*/
