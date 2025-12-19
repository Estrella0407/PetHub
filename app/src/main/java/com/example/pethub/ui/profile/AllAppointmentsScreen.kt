package com.example.pethub.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAppointmentsScreen(
    viewModel: AllAppointmentsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAppointmentClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AllAppointmentsTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is AllAppointmentsUiState.Loading -> LoadingScreen()
                is AllAppointmentsUiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
                is AllAppointmentsUiState.Success -> {
                    AllAppointmentsContent(
                        appointments = state.appointments,
                        onAppointmentClick = onAppointmentClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllAppointmentsTopBar(onNavigateBack: () -> Unit) {
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
private fun AllAppointmentsContent(
    appointments: List<AppointmentItem>,
    onAppointmentClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
                    text = "All Appointments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DarkBrown.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                if (appointments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No appointments found",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(appointments) { appointment ->
                            AppointmentListItem(
                                appointment = appointment,
                                onClick = { onAppointmentClick(appointment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentListItem(
    appointment: AppointmentItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Icon/Indicator
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = CreamDark.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = CreamDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Appointment Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.serviceName ?: "Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appointment.pet.petName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appointment.dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = appointment.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (appointment.status.lowercase()) {
                    "confirmed" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    "pending" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                    "cancelled" -> Color(0xFFF44336).copy(alpha = 0.2f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                }
            ) {
                Text(
                    text = appointment.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (appointment.status.lowercase()) {
                        "confirmed" -> Color(0xFF4CAF50)
                        "pending" -> Color(0xFFFF9800)
                        "cancelled" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                )
            }
        }
    }
}
