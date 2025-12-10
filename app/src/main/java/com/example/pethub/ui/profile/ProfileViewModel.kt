package com.example.pethub.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.Customer
import com.example.pethub.data.model.Pet
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.CustomerRepository
import com.example.pethub.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine // 👈 Make sure this is imported
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val appointmentRepository: AppointmentRepository,
    //private val orderRepository: OrderRepository,
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()


    init {
        loadCustomerData()
    }

    fun loadCustomerData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // Use 'combine' to merge multiple data streams into one.
                // This block will run every time customer, appointments, or pets data changes.
                combine(
                    customerRepository.listenToCurrentCustomer(),
                    appointmentRepository.getUpcomingAppointments(limit = 2), // Fetches appointments
                    petRepository.getPetsForCurrentUser()                      // Fetches pets
                ) { customer: Customer?, appointments: List<Appointment>, pets: List<Pet> ->
                    // Create a new Success state with all the latest data
                    ProfileUiState.Success(
                        customer = customer,
                        appointments = appointments,
                        pets = pets
                    )
                }.collect { combinedState ->
                    // Update the UI with the complete, combined state
                    _uiState.value = combinedState
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            // check current state just for the initial check
            if (_uiState.value is ProfileUiState.Success) {
                try {
                    // Start Uploading
                    _uiState.update { state ->
                        if (state is ProfileUiState.Success) {
                            state.copy(isUploading = true, uploadProgress = 0f)
                        } else state
                    }

                    customerRepository.uploadProfileImage(uri) { progress ->
                        // Update Progress safely
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                state.copy(uploadProgress = progress)
                            } else state
                        }
                    }.onSuccess { imageUrl ->
                        // Update Firestore
                        customerRepository.updateProfileImageUrl(imageUrl)

                        // Stop uploading.
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                state.copy(isUploading = false)
                            } else state
                        }
                    }.onFailure { e ->
                        // Handle failure safely
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                ProfileUiState.Error("Upload failed: ${e.message}", customer = state.customer)
                            } else state
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception safely
                    _uiState.update { state ->
                        if (state is ProfileUiState.Success) {
                            ProfileUiState.Error("Upload failed", customer = state.customer)
                        } else state
                    }
                }
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

sealed class ProfileUiState(open val customer: Customer? = null) {
    data object Loading : ProfileUiState()

    data class Success(
        override val customer: Customer?,
        val appointments: List<Appointment> = emptyList(),
        //val orders: List<Order> = emptyList(),
        val pets: List<Pet> = emptyList(),
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f
    ) : ProfileUiState(customer)

    data class Error(
        val message: String,
        override val customer: Customer? = null
    ) : ProfileUiState(customer)
}
