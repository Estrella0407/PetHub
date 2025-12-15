package com.example.pethub.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pethub.navigation.AdminBottomNavigationBar
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamFair
import com.example.pethub.ui.theme.MutedBrown

@Composable
fun AdminViewAllAppointmentsScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToStocks: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onViewAppointmentClick: (String) -> Unit
    ){
    val uiState by viewModel.uiState.collectAsState()
    val appointments by viewModel.recentAppointments.collectAsState()
    Scaffold(
        topBar = {
            AdminTopBar(
                onLogoutClick = {
                    viewModel.logout(onLogoutSuccess = onNavigateToLogin)
                }
            )
        },
        bottomBar = {
            AdminBottomNavigationBar(
                modifier = Modifier,
                currentRoute = "admin_home",
                onNavigate = { route ->
                    when (route) {
                        "admin_home" -> onNavigateToHome()
                        "admin_stocks" -> onNavigateToStocks()
                        "admin_services" -> onNavigateToServices()
                        "admin_scanner" -> onNavigateToScanner()
                    }
                }
            )
        },
        containerColor = CreamBackground // Using your theme color
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "All Appointments",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(horizontal=24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    AppointmentFullList(
                        appointments = appointments,
                        onViewClick = onViewAppointmentClick
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentFullList(
    appointments: List<AdminAppointment>,
    onViewClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CreamFair),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            appointments.forEachIndexed { index, appt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${appt.id.take(5)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = appt.date,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "view",
                        textDecoration = TextDecoration.Underline,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.clickable { onViewClick(appt.id) }
                    )
                }

                if (index < appointments.take(4).size - 1) {
                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}