package com.example.pethub.ui.admin

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.pethub.data.model.toServiceItem
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// Data classes for the report
data class ServiceUsage(
    val serviceName: String,
    val count: Int
)

data class ServiceUsageReportData(
    val usageByService: List<ServiceUsage> = emptyList(),
    // FIX: Change BarChartData to PieChartData
    val pieChartData: PieChartData? = null
)

// UI State for the screen
sealed class ServiceUsageReportUiState {
    object Loading : ServiceUsageReportUiState()
    data class Success(val reportData: ServiceUsageReportData) : ServiceUsageReportUiState()
    data class Error(val message: String) : ServiceUsageReportUiState()
}

@HiltViewModel
class ServiceUsageReportViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceUsageReportUiState>(ServiceUsageReportUiState.Loading)
    val uiState: StateFlow<ServiceUsageReportUiState> = _uiState.asStateFlow()

    private val _availableMonths = MutableStateFlow<List<String>>(emptyList())
    val availableMonths: StateFlow<List<String>> = _availableMonths.asStateFlow()

    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    init {
        generateMonthList()
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        _selectedMonth.value = currentMonth
        loadServiceUsageReport(currentMonth)
    }

    private fun generateMonthList() {
        val months = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        for (i in 0..11) {
            months.add(format.format(calendar.time))
            calendar.add(Calendar.MONTH, -1)
        }
        _availableMonths.value = months
    }

    fun onMonthSelected(month: String) {
        _selectedMonth.value = month
        loadServiceUsageReport(month)
    }

    fun loadServiceUsageReport(month: String) {
        viewModelScope.launch {
            _uiState.value = ServiceUsageReportUiState.Loading
            try {
                val servicesFromRepo = serviceRepository.listenToServices().first()
                val serviceItemMap = servicesFromRepo.map { it.toServiceItem() }.associateBy { it.id }

                val result = appointmentRepository.getAllAppointments()
                result.onSuccess { appointments ->
                    val completedAppointments = appointments.filter { it.status == "Completed" }

                    val utcTimeZone = TimeZone.getTimeZone("UTC")
                    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    sdf.timeZone = utcTimeZone

                    val selectedCalendar = Calendar.getInstance(utcTimeZone)
                    selectedCalendar.time = sdf.parse(month)!!

                    val monthAppointments = completedAppointments.filter { appt ->
                        appt.dateTime?.let {
                            val apptCalendar = Calendar.getInstance(utcTimeZone)
                            apptCalendar.time = it.toDate()
                            apptCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                                    apptCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
                        } ?: false
                    }

                    val completedServicesInMonth = monthAppointments.mapNotNull { appt ->
                        serviceItemMap[appt.serviceId]
                    }

                    val usageByService = completedServicesInMonth
                        .groupBy { it.serviceName }
                        .map { (name, serviceItems) ->
                            ServiceUsage(name, serviceItems.size)
                        }
                        .sortedByDescending { it.count }

                    val totalUsage = usageByService.sumOf { it.count }

                    // --- PIE CHART DATA PREPARATION ---
                    val pieChartData = if (totalUsage > 0) {
                        val chartColors = listOf(
                            Color(0xFF5E454B), Color(0xFF8A6B6D), Color(0xFFC0A19A),
                            Color(0xFFD3B8AE), Color(0xFFE2DCC8), Color(0xFFA2847E)
                        )
                        val slices = usageByService.mapIndexed { index, usage ->
                            PieChartData.Slice(
                                label = usage.serviceName,
                                value = (usage.count.toFloat() / totalUsage.toFloat()),
                                color = chartColors[index % chartColors.size]
                            )
                        }
                        PieChartData(slices = slices, plotType = PlotType.Pie)
                    } else null

                    val reportData = ServiceUsageReportData(
                        usageByService = usageByService,
                        pieChartData = pieChartData // Assign the new pie chart data
                    )
                    _uiState.value = ServiceUsageReportUiState.Success(reportData)

                }.onFailure { e ->
                    _uiState.value = ServiceUsageReportUiState.Error(e.message ?: "Failed to load report.")
                }
            } catch (e: Exception) {
                _uiState.value = ServiceUsageReportUiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
}
