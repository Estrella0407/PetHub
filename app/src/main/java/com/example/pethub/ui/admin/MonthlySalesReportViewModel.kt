package com.example.pethub.ui.admin

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// Data classes for the report
data class ServiceRevenue(
    val serviceName: String,
    val revenue: Double
)

// Main data holder for the screen
data class SalesReportData(
    val totalRevenue: Double = 0.0,
    val revenueByService: List<ServiceRevenue> = emptyList(),
    val pieChartData: PieChartData? = null // Data for the pie chart
)

// UI State for the screen
sealed class SalesReportUiState {
    object Loading : SalesReportUiState()
    data class Success(val reportData: SalesReportData) : SalesReportUiState()
    data class Error(val message: String) : SalesReportUiState()
}

@HiltViewModel
class MonthlySalesReportViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SalesReportUiState>(SalesReportUiState.Loading)
    val uiState: StateFlow<SalesReportUiState> = _uiState.asStateFlow()

    private val _availableMonths = MutableStateFlow<List<String>>(emptyList())
    val availableMonths: StateFlow<List<String>> = _availableMonths.asStateFlow()

    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    init {
        generateMonthList()
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        _selectedMonth.value = currentMonth
        loadMonthlySalesReport(currentMonth)
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
        loadMonthlySalesReport(month)
    }

    private fun loadMonthlySalesReport(month: String) {
        viewModelScope.launch {
            _uiState.value = SalesReportUiState.Loading
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

                    val totalRevenue = completedServicesInMonth.sumOf { it.price }
                    val revenueByService = completedServicesInMonth
                        .groupBy { it.serviceName }
                        .map { (name, serviceItems) ->
                            ServiceRevenue(name, serviceItems.sumOf { it.price })
                        }
                        .sortedByDescending { it.revenue }

                    // --- PIE CHART DATA PREPARATION ---
                    val pieChartData = if (totalRevenue > 0) {
                        val chartColors = listOf(
                            Color(0xFF5E454B),
                            Color(0xFF8A6B6D),
                            Color(0xFFC0A19A),
                            Color(0xFFD3B8AE),
                            Color(0xFFE2DCC8),
                            Color(0xFFA2847E)
                        )
                        val slices = revenueByService.mapIndexed { index, service ->
                            PieChartData.Slice(
                                label = service.serviceName,
                                value = (service.revenue / totalRevenue).toFloat(),
                                color = chartColors[index % chartColors.size]
                            )
                        }
                        PieChartData(slices = slices, plotType = PlotType.Pie)
                    } else null

                    val reportData = SalesReportData(
                        totalRevenue = totalRevenue,
                        revenueByService = revenueByService,
                        pieChartData = pieChartData // Add chart data to state
                    )
                    _uiState.value = SalesReportUiState.Success(reportData)

                }.onFailure { e ->
                    _uiState.value = SalesReportUiState.Error(e.message ?: "Failed to load sales report.")
                }
            } catch (e: Exception) {
                _uiState.value = SalesReportUiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("ms", "MY"))
        return format.format(amount).replace("RM", "RM ")
    }
}
