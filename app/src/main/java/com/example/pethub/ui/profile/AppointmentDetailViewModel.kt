package com.example.pethub.ui.profile

import androidx.lifecycle.SavedStateHandle
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

    // Remove the init block and SavedStateHandle

    fun loadAppointmentDetails(id: String) {
        if (id.isBlank()) {
            _uiState.update { CustomerAppointmentUiState.Error("Invalid Appointment ID.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { CustomerAppointmentUiState.Loading }
            try {
                val result = appointmentRepository.getAppointmentById(id)
                val appointment = result.getOrElse { throw it ?: Exception("Failed to load appointment") }

                if (appointment != null) {
                    _uiState.update { CustomerAppointmentUiState.Success(appointment) }
                } else {
                    _uiState.update { CustomerAppointmentUiState.Error("Appointment not found.") }
                }
            } catch (e: Exception) {
                _uiState.update { CustomerAppointmentUiState.Error(e.message ?: "An unknown error occurred") }
            }
        }
    }
}