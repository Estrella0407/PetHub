package com.example.pethub.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    viewModel: AppointmentDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = appointmentId) {
        viewModel.loadAppointmentDetails(appointmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            when (val state = uiState) {
                is CustomerAppointmentUiState.Loading -> LoadingScreen()
                is CustomerAppointmentUiState.Error -> ErrorScreen(message = state.message, onRetry = { viewModel.loadAppointmentDetails(appointmentId) })
                is CustomerAppointmentUiState.Success -> {
                    AppointmentDetailContent(appointment = state.appointmentItem)
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailContent(appointment: AppointmentItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamFair),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Appointment #${appointment.id.take(6)}...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DarkBrown)
            HorizontalDivider(color = MutedBrown)
            DetailRow(label = "Status", value = appointment.status)
            DetailRow(label = "Service", value = appointment.serviceName ?: "N/A")
            DetailRow(label = "For Pet", value = appointment.pet.petName)
            DetailRow(label = "At Branch", value = appointment.locationName)
            DetailRow(label = "Date & Time", value = appointment.dateTime)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = MutedBrown,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Normal,
            color = DarkBrown,
            modifier = Modifier.weight(1f)
        )
    }
}
