package com.example.pethub.ui.appointment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.DarkBrown
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    viewModel: BookAppointmentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.bookingSuccess) {
        if (uiState.bookingSuccess) {
            onBookingSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground, titleContentColor = DarkBrown)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error ?: "An unknown error occurred.")
            }
        } else {
            AppointmentContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onDateSelected = viewModel::onDateSelected,
                onMonthChanged = viewModel::onMonthChanged,
                onTimeSlotSelected = viewModel::onTimeSlotSelected,
                onSpecialInstructionsChanged = viewModel::onSpecialInstructionsChanged,
                onConfirmBooking = viewModel::confirmBooking
            )
        }
    }
}

@Composable
fun AppointmentContent(
    modifier: Modifier = Modifier,
    uiState: BookAppointmentUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (Boolean) -> Unit,
    onTimeSlotSelected: (LocalTime) -> Unit,
    onSpecialInstructionsChanged: (String) -> Unit,
    onConfirmBooking: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
    ) {
        item {
            // Simplified details section, no need for dropdowns here.
            BookingSummarySection(
                serviceName = uiState.service?.serviceName ?: "N/A",
                petName = uiState.selectedPet?.petName ?: "N/A",
                branchName = uiState.selectedBranch?.branchName ?: "N/A"
            )
        }

        item {
            OutlinedTextField(
                value = uiState.specialInstructions,
                onValueChange = onSpecialInstructionsChanged,
                label = { Text("Special Instructions (Optional)") },
                placeholder = { Text("e.g., sensitive to loud noises") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBrown,
                    unfocusedBorderColor = CreamDark
                )
            )
        }

        item {
            CalendarSection(
                currentDisplayMonth = uiState.currentDisplayMonth,
                selectedDate = uiState.selectedDate,
                onDateSelected = onDateSelected,
                onMonthChanged = onMonthChanged
            )
        }

        item {
            TimeSlotsSection(
                selectedDate = uiState.selectedDate,
                timeSlots = uiState.availableTimeSlots,
                selectedTimeSlot = uiState.selectedTimeSlot,
                onTimeSlotSelected = onTimeSlotSelected
            )
        }

        item {
            Button(
                onClick = onConfirmBooking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBrown), // Stronger CTA color
                enabled = !uiState.bookingInProgress && uiState.selectedTimeSlot != null
            ) {
                if (uiState.bookingInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Confirm Booking", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

// SIMPLIFIED: This section just displays the confirmed details.
@Composable
fun BookingSummarySection(serviceName: String, petName: String, branchName: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.grooming_nobg), // Replace with a generic icon if needed
            contentDescription = "Service Icon",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(serviceName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkBrown)
            Text("Pet: $petName", style = MaterialTheme.typography.bodyLarge, color = DarkBrown)
            Text("Branch: $branchName", style = MaterialTheme.typography.bodyLarge, color = DarkBrown)
        }
    }
}

// NOTE: The CalendarSection and TimeSlotsSection composables can remain the same as in your original file.
// I have removed PetSelection and BranchSelection as they are no longer needed on this screen.
@Composable
fun CalendarSection(
    currentDisplayMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (Boolean) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .background(DarkBrown, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Select a Date",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(false) }) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Previous Month")
            }
            Text(
                text = currentDisplayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = { onMonthChanged(true) }) {
                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Month")
            }
        }
        // Calendar grid implementation would go here...
    }
}

@Composable
fun TimeSlotsSection(
    selectedDate: LocalDate,
    timeSlots: List<TimeSlot>,
    selectedTimeSlot: LocalTime?,
    onTimeSlotSelected: (LocalTime) -> Unit
) {
    Column {
        Text(
            "Available Slots for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
            fontWeight = FontWeight.Bold,
            color = DarkBrown,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Time slot grid implementation would go here...
    }
}
