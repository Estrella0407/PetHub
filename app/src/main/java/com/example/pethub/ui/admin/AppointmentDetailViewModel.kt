package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppointmentDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            appointmentRepository.getAppointmentDetail(appointmentId)
                .onSuccess { appointment ->
                    _uiState.value = _uiState.value.copy(
                        appointment = appointment,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to load appointment",
                        isLoading = false
                    )
                }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onLogoutSuccess()
        }
    }
}


data class AppointmentDetailUiState(
    val isLoading: Boolean = false,
    val appointment: Appointment? = null,
    val errorMessage: String = ""
)
