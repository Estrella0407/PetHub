package com.example.pethub.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    viewModel: ServiceManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetHub") },
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ServiceManagementUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.services) { service ->
                            ServiceRow(
                                service = service,
                                onCheckedChange = { isEnabled ->
                                    viewModel.updateServiceAvailability(service.serviceId, isEnabled)
                                }
                            )
                        }
                    }
                }
                is ServiceManagementUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceRow(
    service: ServiceManagementItem,
    onCheckedChange: (Boolean) -> Unit
) {
    val iconRes = when (service.name) {
        "Grooming" -> R.drawable.grooming_nobg
        "Boarding" -> R.drawable.boarding_nobg
        "Walking" -> R.drawable.walking_nobg
        "DayCare" -> R.drawable.daycare_nobg
        "Training" -> R.drawable.training_nobg
        else -> R.drawable.pethub_rvbg // A default icon
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
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = service.name, fontSize = 18.sp)
            }
            Switch(
                checked = service.isEnabled,
                onCheckedChange = onCheckedChange
            )
        }
        Divider()
    }
}
