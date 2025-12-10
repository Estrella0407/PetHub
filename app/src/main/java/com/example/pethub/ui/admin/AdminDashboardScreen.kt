package com.example.pethub.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R // Make sure to import your R file
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.CreamLight
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.pethub_logo_rvbg),
                        contentDescription = "PetHub Logo",
                        modifier = Modifier.height(40.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground,
                    navigationIconContentColor = CreamDark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Profile Header ---
            Image(
                painter = painterResource(id = R.drawable.pethub_logo_rvbg),
                contentDescription = "Admin Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, CreamDark, CircleShape)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "PetHub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = CreamDark
                )
                MonthDropdown(
                    selectedMonth = uiState.selectedMonth,
                    onMonthSelected = { viewModel.onMonthSelected(it) }
                )
            }

            // --- Monthly Sales Report Card ---
            ReportCard(
                title = "Monthly Sales Report",
                // Pass the ServiceDropdown to be displayed inside the card
                headerAction = {
                    ServiceDropdown(
                        selectedService = uiState.selectedService,
                        onServiceSelected = { viewModel.onServiceSelected(it) }
                    )
                }
            ) {
                val salesReport = uiState.salesReport
                // The content of the card starts here
                Text(
                    "Total Revenue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = CreamDark
                )
                Text(
                    formatCurrency(salesReport.totalRevenue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = CreamDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                salesReport.salesByService.forEach { (service, amount) ->
                    Text(
                        "• $service - ${formatCurrency(amount)}",
                        fontSize = 16.sp,
                        color = CreamDark
                    )
                }
            }

            // --- Service Usage Report Card (does not have a header action) ---
            ReportCard(title = "Service Usage Report") {
                val usageReport = uiState.usageReport
                usageReport.usageByService.forEach { (service, times) ->
                    Text(
                        "• $service - $times times",
                        fontSize = 16.sp,
                        color = CreamDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Dropdown for selecting the month
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDropdown(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf("This month", "Last month", "January", "February", "March") // Add more as needed

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CreamDark),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(selectedMonth, color = Color.White)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Open month selection",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
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

// DROPDOWN for selecting the service
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDropdown(
    selectedService: String,
    onServiceSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val services = listOf("Training", "Boarding", "Grooming", "Walking", "Daycare")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CreamDark),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(selectedService, color = Color.White)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Open service selection",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            services.forEach { service ->
                DropdownMenuItem(
                    text = { Text(service) },
                    onClick = {
                        onServiceSelected(service)
                        expanded = false
                    }
                )
            }
        }
    }
}


// *** THIS IS THE CORRECTED ReportCard COMPOSABLE ***
@Composable
private fun ReportCard(
    title: String,
    headerAction: (@Composable () -> Unit)? = null, // Optional composable for the dropdown
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // If a header action (the dropdown) is provided, display it here, aligned to the right.
            headerAction?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    it()
                }
                // Add a small spacer between the dropdown and the title
                Spacer(modifier = Modifier.height(8.dp))
            }

            // The main title of the card
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = CreamDark,
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier = Modifier.height(12.dp))
            // The rest of the card's content (Total Revenue, etc.) goes below
            content()
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("ms", "MY"))
    return format.format(amount).replace("MYR", "RM")
}
