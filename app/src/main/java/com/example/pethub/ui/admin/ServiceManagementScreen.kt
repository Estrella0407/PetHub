package com.example.pethub.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pethub.data.model.Service
import com.example.pethub.navigation.AdminBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceManagementScreen(
    onNavigateToAdminHome: () -> Unit,
    onNavigateToAdminStocks: () -> Unit,
    onNavigateToAdminScanner: () -> Unit
) {
    // Dummy list of services based on the image
    val services = listOf(
        Service(serviceName = "Grooming"),
        Service(serviceName = "Boarding"),
        Service(serviceName = "Walking"),
        Service(serviceName = "DayCare"),
        Service(serviceName = "Training")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetHub") },
                actions = {
                    IconButton(onClick = { /* TODO: Logout */ }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            AdminBottomNavigationBar(
                modifier = Modifier.fillMaxWidth(),
                currentRoute = "admin_home",
                onNavigate = { route ->
                    when (route) {
                        "admin_home" -> onNavigateToAdminHome
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(services) { service ->
                    ServiceRow(service = service)
                }
            }
        }
    }
}

@Composable
fun ServiceRow(service: Service) {
    var isEnabled by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder for the icon
                Image(
                    imageVector = Icons.Default.Pets,
                    contentDescription = service.serviceName,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = service.serviceName, fontSize = 18.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { isEnabled = it }
            )
        }
        Divider()
    }
}
