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
import co.yml.charts.ui.piechart.models.PieChartConfig.LabelType
import com.example.pethub.R
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamFair
import com.example.pethub.ui.theme.DarkBrown
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceUsageReportScreen(
    viewModel: ServiceUsageReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    Scaffold(
        topBar = {
            ServiceUsageReportTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is ServiceUsageReportUiState.Loading -> LoadingScreen()
                is ServiceUsageReportUiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.onMonthSelected(selectedMonth) }
                )
                is ServiceUsageReportUiState.Success -> {
                    ServiceUsageReportContent(
                        reportData = state.reportData,
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
private fun ServiceUsageReportTopBar(onNavigateBack: () -> Unit) {
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
private fun ServiceUsageReportContent(
    reportData: ServiceUsageReportData,
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
                    text = "Service Usage Report",
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
                // --- END OF PIE CHART SECTION ---

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = DarkBrown, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Times Used By Service",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (reportData.usageByService.isEmpty()) {
                    Text(
                        text = "No services used in this month.",
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        color = DarkBrown.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    reportData.usageByService.forEach { serviceUsage ->
                        ServiceUsageRow(
                            serviceName = serviceUsage.serviceName,
                            count = serviceUsage.count
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceUsageRow(serviceName: String, count: Int) {
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
            text = "$count times",
            color = DarkBrown,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
