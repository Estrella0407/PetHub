package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Admin Dashboard
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    // Temporary mock data for appointments (Replace with Repository call later)
    private val _recentAppointments = MutableStateFlow<List<AdminAppointment>>(emptyList())
    val recentAppointments: StateFlow<List<AdminAppointment>> = _recentAppointments.asStateFlow()

    init {
        loadData()
    }

   fun loadData() {
       viewModelScope.launch {
           _uiState.value = AdminDashboardUiState.Loading

           val result = appointmentRepository.getAllAppointments()

           result
               .onSuccess { appointments ->
                   _recentAppointments.value = appointments.map {
                       AdminAppointment(
                           id = it.appointmentId,
                           date = it.dateTime?.toDate()?.let { date ->
                               SimpleDateFormat(
                                   "dd MMM yyyy",
                                   Locale.getDefault()
                               ).format(date)
                           } ?: ""
                       )
                   }
                   _uiState.value = AdminDashboardUiState.Success
               }
               .onFailure { e ->
                   _uiState.value =
                       AdminDashboardUiState.Error(e.message ?: "Failed to load data")
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

// Sealed class for UI State
sealed class AdminDashboardUiState {
    object Loading : AdminDashboardUiState()
    object Success : AdminDashboardUiState()
    data class Error(val message: String) : AdminDashboardUiState()
}

// Simple data class for the list item
data class AdminAppointment(
    val id: String,
    val date: String
)
