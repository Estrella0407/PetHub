package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<AdminProfileUiState> = _uiState.asStateFlow()

    private fun createInitialState(): AdminProfileUiState {
        // Hardcoded data for "This month"
        val sales = SalesReport(
            totalRevenue = 5200.00,
            salesByService = mapOf(
                "Grooming" to 3000.00,
                "Boarding" to 1500.00,
                "Daycare" to 500.00,
                "Walking" to 200.00
            )
        )
        val usage = UsageReport(
            usageByService = mapOf(
                "Grooming" to 50,
                "Boarding" to 15,
                "Daycare" to 10,
                "Walking" to 5
            )
        )
        return AdminProfileUiState(salesReport = sales, usageReport = usage)
    }

    fun onMonthSelected(month: String) {
        _uiState.update { it.copy(selectedMonth = month) }
        //TODO: Fetch new data based on the selected month

    }
    fun onServiceSelected(service: String) {
        _uiState.update { it.copy(selectedService = service) }
        //TODO: Fetch new data based on the selected service
    }
}

// Hardcoded data models for the reports
data class SalesReport(
    val totalRevenue: Double,
    val salesByService: Map<String, Double>
)

data class UsageReport(
    val usageByService: Map<String, Int>
)

// UI State for the screen
data class AdminProfileUiState(
    val selectedMonth: String = "This month",
    val salesReport: SalesReport,
    val selectedService: String = "By Service",
    val usageReport: UsageReport,
    val isLoading: Boolean = false
)