package com.example.pethub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AllAppointmentsUiState {
    object Loading : AllAppointmentsUiState()
    data class Success(val appointments: List<AppointmentItem>) : AllAppointmentsUiState()
    data class Error(val message: String) : AllAppointmentsUiState()
}

@HiltViewModel
class AllAppointmentsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AllAppointmentsUiState>(AllAppointmentsUiState.Loading)
    val uiState: StateFlow<AllAppointmentsUiState> = _uiState.asStateFlow()

    init {
        loadAllAppointments()
    }

    private fun loadAllAppointments() {
        viewModelScope.launch {
            try {
                _uiState.value = AllAppointmentsUiState.Loading

                appointmentRepository.getAllAppointmentsForCurrentUser()
                    .map { appointments ->
                        appointments.mapNotNull { appointment ->
                            appointmentRepository.getAppointmentItem(appointment).getOrNull()
                        }
                    }
                    .collect { appointmentItems ->
                        _uiState.value = AllAppointmentsUiState.Success(appointmentItems)
                    }
            } catch (e: Exception) {
                _uiState.value = AllAppointmentsUiState.Error(
                    e.message ?: "Failed to load appointments"
                )
            }
        }
    }

    fun retry() {
        loadAllAppointments()
    }
}

