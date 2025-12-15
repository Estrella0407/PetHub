package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Admin Dashboard
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
            try {
                // TODO: Fetch actual appointments from Firestore
                // For now, mocking the data based on your screenshot
                _recentAppointments.value = listOf(
                    AdminAppointment("IT001", "29 Sept 2025"),
                    AdminAppointment("PG002", "28 Sept 2025"),
                    AdminAppointment("PW001", "25 Sept 2025"),
                    AdminAppointment("PG001", "25 Sept 2025"),
                    AdminAppointment("IT002", "24 Sept 2025"),
                    AdminAppointment("IT003", "23 Sept 2025")
                )
                _uiState.value = AdminDashboardUiState.Success
            } catch (e: Exception) {
                _uiState.value = AdminDashboardUiState.Error(e.message ?: "Unknown error")
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
