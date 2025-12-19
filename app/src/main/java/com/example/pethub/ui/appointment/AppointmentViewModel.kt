package com.example.pethub.ui.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.R
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.BranchService
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.BranchRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for the entire Appointment Screen
data class AppointmentUiState(
    val serviceName: String = "",
    val serviceImageRes: Int = 0,
    val availableBranches: List<Branch> = emptyList(),
    val isLoading: Boolean = true,
    val bookingSuccess: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppointmentUiState>(AppointmentUiState())
    val uiState = _uiState.asStateFlow()

    fun bookAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                appointmentRepository.createAppointment(appointment)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bookingSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Booking failed"
                    )
                }
            }
        }
    }


    private fun createInitialState(serviceId: String): AppointmentUiState {
        return when (serviceId) {
            "grooming" -> AppointmentUiState(serviceName = "Grooming", serviceImageRes = R.drawable.grooming_nobg)
            "boarding" -> AppointmentUiState(serviceName = "Boarding", serviceImageRes = R.drawable.boarding_nobg)
            "walking" -> AppointmentUiState(serviceName = "Walking", serviceImageRes = R.drawable.walking_nobg)
            "daycare" -> AppointmentUiState(serviceName = "Daycare", serviceImageRes = R.drawable.daycare_nobg)
            "training" -> AppointmentUiState(serviceName = "Training", serviceImageRes = R.drawable.training_nobg)
            else -> AppointmentUiState(serviceName = "Service")
        }
    }
}
