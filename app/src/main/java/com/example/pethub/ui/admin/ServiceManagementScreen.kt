package com.example.pethub.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.navigation.AdminBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceManagementScreen(
    onNavigateToAdminHome: () -> Unit,
    onNavigateToAdminStocks: () -> Unit,
    onNavigateToAdminScanner: () -> Unit,
    viewModel: ServiceManagementViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

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
                modifier = Modifier.fillMaxWidth(),
                currentRoute = "admin_services",
                onNavigate = { route ->
                    when (route) {
                        "admin_home" -> onNavigateToAdminHome()
                        "admin_stocks" -> onNavigateToAdminStocks()
                        "admin_services" -> { /* Stay */ }
                        "admin_scanner" -> onNavigateToAdminScanner()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Service Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is ServiceManagementUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is ServiceManagementUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is ServiceManagementUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.services) { service ->
                            ServiceManagementRow(
                                service = service,
                                onToggle = { isAvailable ->
                                    viewModel.updateServiceAvailability(service.serviceId, isAvailable)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceManagementRow(
    service: ServiceManagementItem,
    onToggle: (Boolean) -> Unit
) {
    val iconRes = when (service.serviceId) {
        "grooming" -> R.drawable.grooming_nobg
        "boarding" -> R.drawable.boarding_nobg
        "walking" -> R.drawable.walking_nobg
        "daycare" -> R.drawable.daycare_nobg
        "training" -> R.drawable.training_nobg
        else -> R.drawable.pethub_rvbg
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = service.name,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = service.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
            Switch(
                checked = service.isEnabled,
                onCheckedChange = onToggle
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
