package com.example.pethub.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartConfig.LabelType // Import LabelType
import com.example.pethub.R
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamFair
import com.example.pethub.ui.theme.DarkBrown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySalesReportScreen(
    viewModel: MonthlySalesReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    Scaffold(
        topBar = {
            SalesReportTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is SalesReportUiState.Loading -> LoadingScreen()
                is SalesReportUiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.onMonthSelected(selectedMonth) }
                )
                is SalesReportUiState.Success -> {
                    SalesReportContent(
                        reportData = state.reportData,
                        formatCurrency = viewModel::formatCurrency,
                        availableMonths = availableMonths,
                        selectedMonth = selectedMonth,
                        onMonthSelected = viewModel::onMonthSelected
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesReportTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pethub_rvbg),
                    contentDescription = "PetHub Logo",
                    modifier = Modifier.height(40.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(48.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CreamBackground,
            navigationIconContentColor = DarkBrown
        )
    )
}

@Composable
private fun SalesReportContent(
    reportData: SalesReportData,
    formatCurrency: (Double) -> String,
    availableMonths: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            MonthDropdown(
                selectedMonth = selectedMonth,
                availableMonths = availableMonths,
                onMonthSelected = onMonthSelected
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CreamFair),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Monthly Sales Report",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DarkBrown.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                // --- PIE CHART SECTION ---
                reportData.pieChartData?.let { pieData ->
                    PieChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        pieChartData = pieData,
                        pieChartConfig = PieChartConfig(
                            backgroundColor = CreamFair,
                            isAnimationEnable = true,
                            animationDuration = 800,
                            chartPadding = 40,
                            isSumVisible = false,
                            labelType = LabelType.PERCENTAGE,
                            labelVisible = true,
                            sliceLabelTextSize = 12.sp,
                            sliceLabelTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = DarkBrown, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Total Revenue",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatCurrency(reportData.totalRevenue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkBrown
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Revenue By Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (reportData.revenueByService.isEmpty()) {
                    Text(
                        text = "No sales recorded for this month.",
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        color = DarkBrown.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    reportData.revenueByService.forEach { serviceRevenue ->
                        RevenueRow(
                            serviceName = serviceRevenue.serviceName,
                            revenue = formatCurrency(serviceRevenue.revenue)
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun MonthDropdown(
    selectedMonth: String,
    availableMonths: List<String>,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val currentMonthFormatted = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
    }
    val buttonText = if (selectedMonth == currentMonthFormatted) {
        "This month"
    } else {
        selectedMonth
    }

    Box {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkBrown,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select Month",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = buttonText, fontSize = 12.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableMonths.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RevenueRow(serviceName: String, revenue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "•", modifier = Modifier.padding(end = 8.dp), color = DarkBrown)
            Text(
                text = serviceName,
                color = DarkBrown,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            HorizontalDivider(
                color = DarkBrown.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = revenue,
            color = DarkBrown,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
