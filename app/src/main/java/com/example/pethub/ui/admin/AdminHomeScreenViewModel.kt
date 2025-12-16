package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.ui.auth.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdminHomeScreenViewModel @Inject constructor(
    private val repository: AppointmentRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(AdminHomeScreenUiState())
    val uiState: StateFlow<AdminHomeScreenUiState> = _uiState.asStateFlow()


    private fun loadAppointments(){
        viewModelScope.launch{
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getAllAppointments()
                .onSuccess {
                    _uiState.value = _uiState.value.copy (
                        appointments = it,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to retrieve appointments data.",
                        isLoading = false
                    )
                }
        }
    }
}

data class AdminHomeScreenUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val appointments: List<Appointment> = emptyList()
)