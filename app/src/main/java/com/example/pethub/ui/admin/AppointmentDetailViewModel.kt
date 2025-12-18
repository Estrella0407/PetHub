package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

            appointmentRepository.getAppointmentDetail(appointmentId) //This SUCCESS
                .onSuccess { appointment ->
                    if (appointment == null) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Appointment not found",
                            isLoading = false
                        )
                        return@onSuccess
                    }
                    _uiState.value = _uiState.value.copy(
                        appointment = appointment,
                        isLoading = false
                    )

                    appointmentRepository.getAppointmentItem(appointment)
                        .onSuccess { appointmentItem -> // This failed
                            println("DEBUG appointmentItem = $appointmentItem")
                            _uiState.value = _uiState.value.copy(
                                appointment = appointment,
                                appointmentItem = appointmentItem,
                                isLoading = false
                            )
                        }
                        .onFailure {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Failed to load appointment item",
                                isLoading = false
                            )
                        }

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
    val appointmentItem: AppointmentItem? = null,
    val errorMessage: String = ""
)
