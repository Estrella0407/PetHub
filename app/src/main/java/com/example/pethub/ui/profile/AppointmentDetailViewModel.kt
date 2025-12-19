package com.example.pethub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CustomerAppointmentUiState {
    object Loading : CustomerAppointmentUiState()
    data class Success(val appointmentItem: AppointmentItem) : CustomerAppointmentUiState()
    data class Error(val message: String) : CustomerAppointmentUiState()
}

@HiltViewModel
class AppointmentDetailViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerAppointmentUiState>(CustomerAppointmentUiState.Loading)
    val uiState: StateFlow<CustomerAppointmentUiState> = _uiState.asStateFlow()

    // No need for SavedStateHandle if the ID is passed directly to the function
    fun loadAppointmentDetails(id: String) {
        if (id.isBlank()) {
            _uiState.update { CustomerAppointmentUiState.Error("Invalid Appointment ID.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { CustomerAppointmentUiState.Loading }
            try {
                // Step 1: Fetch the base Appointment object using the correct repository function
                val appointmentResult = appointmentRepository.getAppointmentDetail(id)
                val appointment = appointmentResult.getOrNull()

                if (appointment != null) {
                    // Step 2: Use the fetched Appointment object to get the detailed AppointmentItem
                    val itemResult = appointmentRepository.getAppointmentItem(appointment)
                    val appointmentItem = itemResult.getOrNull()

                    if (appointmentItem != null) {
                        // Step 3: Update the UI with the complete AppointmentItem
                        _uiState.update { CustomerAppointmentUiState.Success(appointmentItem) }
                    } else {
                        _uiState.update { CustomerAppointmentUiState.Error("Could not process appointment details.") }
                    }
                } else {
                    _uiState.update { CustomerAppointmentUiState.Error("Appointment not found.") }
                }
            } catch (e: Exception) {
                _uiState.update { CustomerAppointmentUiState.Error(e.message ?: "An unknown error occurred") }
            }
        }
    }
}
