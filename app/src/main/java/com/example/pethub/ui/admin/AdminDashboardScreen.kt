package com.example.pethub.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.navigation.AdminBottomNavigationBar
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToStocks: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToAppointmentDetails: (String) -> Unit,
    onViewAllClick:() -> Unit
) {
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
                        "admin_home" -> { /* Current */ }
                        "admin_stocks" -> onNavigateToStocks()
                        "admin_services" -> onNavigateToServices()
                        "admin_scanner" -> onNavigateToScanner()
                    }
                }
            )
        },
        containerColor = CreamBackground // Using your theme color
    ) { paddingValues ->
        when (uiState) {
            is AdminDashboardUiState.Loading -> LoadingScreen()
            is AdminDashboardUiState.Error -> ErrorScreen(
                message = (uiState as AdminDashboardUiState.Error).message,
                onRetry = viewModel::loadData
            )
            is AdminDashboardUiState.Success -> {
                AdminDashboardContent(
                    modifier = Modifier.padding(paddingValues),
                    appointments = appointments,
                    onViewAllClick = {onViewAllClick()},
                    onViewAppointmentClick = onNavigateToAppointmentDetails
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(onLogoutClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.pethub_rvbg), // Ensure this resource exists
                contentDescription = "PetHub Logo",
                modifier = Modifier
                    .height(40.dp)
                    .width(120.dp)
            )
        },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = Color.Black
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = CreamBackground
        )
    )
    // Divider line below top bar
    HorizontalDivider(thickness = 2.dp, color = DarkBrown)
}

@Composable
fun AdminDashboardContent(
    modifier: Modifier = Modifier,
    appointments: List<AdminAppointment>,
    onViewAllClick: () -> Unit,
    onViewAppointmentClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // --- New Appointment Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Appointment",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    // Use a cursive-style font if available in your theme, simulating the image
                )
                Text(
                    text = "view all",
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    color = Color.Black,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }

        item {
            AppointmentListCard(
                appointments = appointments,
                onViewClick = onViewAppointmentClick
            )
        }

        // --- Reports Section ---
        item {
            Text(
                text = "Reports",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ReportCard(
                    title = "Monthly Sales\nReport",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Handle Report Click */ }
                )
                ReportCard(
                    title = "Service Usage\nReport",
                    modifier = Modifier.weight(1f),
                    onClick = { /* Handle Report Click */ }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun AppointmentListCard(
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
            // Scrollable list inside the card if there are many items,
            // but usually nested scrolling is tricky. Since we are in a LazyColumn,
            // we'll display a fixed number or column items.
            // For the specific design, a Column is fine.

            appointments.take(4).forEachIndexed { index, appt ->
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

            Text(
                text = "Click \"view all\" to view more appointments",
                fontSize = 12.sp,
                color = MutedBrown,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2DCC8)), // Slightly darker cream/grey
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // "View" Button pill
            Surface(
                shape = RoundedCornerShape(50),
                color = CreamFair,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "View",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}
