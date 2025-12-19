package com.example.pethub.ui.appointment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // <-- IMPORT THIS
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.DarkBrown
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
                onBranchSelected = viewModel::onBranchSelected,
                onSpecialInstructionsChanged = viewModel::onSpecialInstructionsChanged,
                onConfirmBooking = viewModel::confirmBooking,
                onPetSelected = viewModel::onPetSelected

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentContent(
    modifier: Modifier = Modifier,
    uiState: BookAppointmentUiState,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (Boolean) -> Unit,
    onTimeSlotSelected: (LocalTime) -> Unit,
    onBranchSelected: (Branch) -> Unit,
    onSpecialInstructionsChanged: (String) -> Unit,
    onConfirmBooking: () -> Unit,
    onPetSelected: (Pet) -> Unit
) {
    var petDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wrap each section in an item { } block
        item {
            ServiceDetailsSection(
                serviceName = uiState.service?.serviceName ?: "N/A",
                petName = uiState.selectedPet?.petName ?: "N/A",
                serviceType = uiState.service?.type ?: "N/A"
            )
        }

        item {
            PetSelection(
                pets = uiState.userPets,
                selectedPet = uiState.selectedPet,
                expanded = petDropdownExpanded,
                onExpandedChange = { petDropdownExpanded = !petDropdownExpanded },
                onPetSelected = { pet ->
                    onPetSelected(pet)
                    petDropdownExpanded = false
                },
                onDismiss = { petDropdownExpanded = false }
            )
        }

        item {
            OutlinedTextField(
                value = uiState.specialInstructions,
                onValueChange = onSpecialInstructionsChanged,
                label = { Text("Special Instructions") },
                placeholder = { Text("Extra trim around the head.") },
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
            BranchSelection(
                branches = uiState.availableBranches,
                selectedBranch = uiState.selectedBranch,
                onBranchSelected = onBranchSelected
            )
        }

        item {
            Button(
                onClick = onConfirmBooking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0EAD6)),
                enabled = !uiState.bookingInProgress && uiState.selectedTimeSlot != null
            ) {
                if (uiState.bookingInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkBrown)
                } else {
                    Text("Confirm Booking", fontWeight = FontWeight.Bold, color = DarkBrown, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSelection(
    pets: List<Pet>,
    selectedPet: Pet?,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onPetSelected: (Pet) -> Unit,
    onDismiss: () -> Unit
) {
    if (pets.isNotEmpty()) {
        Column {
            Text(
                text = "Select your Pet",
                fontWeight = FontWeight.Bold,
                color = DarkBrown,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandedChange() }
            ) {
                OutlinedTextField(
                    value = selectedPet?.petName ?: "No pet selected",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(), // This is crucial for positioning the dropdown
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBrown,
                        unfocusedBorderColor = CreamDark
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onDismiss
                ) {
                    pets.forEach { pet ->
                        DropdownMenuItem(
                            text = { Text(pet.petName) },
                            onClick = { onPetSelected(pet) }
                        )
                    }
                }
            }
        }
    } else {
        // Optional: Display a helpful message if the user has no pets registered.
        Text(
            "No pets found. Please add a pet to your profile to book an appointment.",
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun ServiceDetailsSection(serviceName: String, petName: String, serviceType: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.grooming_nobg),
            contentDescription = "Grooming Icon",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(serviceName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkBrown)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Pet: $petName", style = MaterialTheme.typography.bodyLarge, color = DarkBrown)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Service Type: $serviceType", style = MaterialTheme.typography.bodyLarge, color = DarkBrown)
        }
    }
}

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
                text = "Appointment Date",
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

        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = DayOfWeek.values()
            for (day in daysOfWeek) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val firstDayOfMonth = currentDisplayMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Make Sunday 0, Monday 1..
        val daysInMonth = currentDisplayMonth.lengthOfMonth()
        val today = LocalDate.now()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp) // Give the grid a fixed height inside the LazyColumn
        ) {
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier.size(40.dp))
            }
            items(daysInMonth) { day ->
                val date = currentDisplayMonth.atDay(day + 1)
                val isSelected = date == selectedDate
                val isToday = date == today
                val isPast = date.isBefore(today)
                DayCell(
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday,
                    isPast = isPast,
                    onClick = { if (!isPast) onDateSelected(it) }
                )
            }
        }
    }
}

@Composable
fun DayCell(date: LocalDate, isSelected: Boolean, isToday: Boolean, isPast: Boolean, onClick: (LocalDate) -> Unit) {
    val backgroundColor = when {
        isSelected -> DarkBrown
        isToday -> CreamDark
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Color.White
        isPast -> Color.Gray.copy(alpha = 0.5f)
        else -> DarkBrown
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = !isPast) { onClick(date) }
            .then(if (isToday && !isSelected) Modifier.border(1.dp, DarkBrown, RoundedCornerShape(8.dp)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text = date.dayOfMonth.toString(), color = textColor, fontWeight = FontWeight.SemiBold)
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
            text = "Time Slots for ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd"))}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DarkBrown
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // Give the grid a fixed height so the parent LazyColumn knows how much space it will take
            modifier = Modifier.height(150.dp)
        ) {
            items(timeSlots) { slot ->
                TimeSlotChip(
                    slot = slot,
                    isSelected = slot.time == selectedTimeSlot,
                    onCLick = onTimeSlotSelected
                )
            }
        }
    }
}

@Composable
fun TimeSlotChip(slot: TimeSlot, isSelected: Boolean, onCLick: (LocalTime) -> Unit) {
    val containerColor = when {
        !slot.isAvailable -> Color(0xFFFFCDD2)
        isSelected -> Color(0xFF00C853)
        else -> Color(0xFF69F0AE)
    }
    val contentColor = if (!slot.isAvailable) Color.Red.copy(alpha = 0.8f) else Color.White
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = slot.isAvailable) { onCLick(slot.time) }
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slot.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 16.sp
            )
            Text(
                text = if (slot.isAvailable) "Available" else "Booked",
                fontSize = 10.sp,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchSelection(
    branches: List<Branch>,
    selectedBranch: Branch?,
    onBranchSelected: (Branch) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Branch", fontWeight = FontWeight.Bold, color = DarkBrown, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedBranch?.branchName ?: "Select a branch",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBrown,
                    unfocusedBorderColor = CreamDark
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                branches.forEach { branch ->
                    DropdownMenuItem(
                        text = { Text(branch.branchName) },
                        onClick = {
                            onBranchSelected(branch)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
