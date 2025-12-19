package com.example.pethub.ui.service

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pethub.R
import com.example.pethub.data.model.Service
import com.example.pethub.navigation.BottomNavigationBar
import com.example.pethub.ui.theme.CreamLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onServiceClick: (String) -> Unit
) {
    // Define the list of main services directly in the composable
    val mainServices = listOf(
        Service(serviceId = "grooming", serviceName = "Grooming", description = "Cleaning and maintaining a pet’s hygiene and appearance."),
        Service(serviceId = "boarding", serviceName = "Boarding", description = "Temporary care for pets when owners are away."),
        Service(serviceId = "walking", serviceName = "Walking", description = "Taking pets out for exercise and bathroom breaks."),
        Service(serviceId = "daycare", serviceName = "Daycare", description = "Daytime care, play, and supervision for pets."),
        Service(serviceId = "training", serviceName = "Training", description = "Teaching pets good behavior and basic commands.")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Services") })
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "services",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "shop" -> onNavigateToShop()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(mainServices, key = { it.serviceId }) { service ->
                ServiceCard(
                    service = service,
                    onClick = { onServiceClick(service.serviceName) }
                )
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getServiceIcon(service.serviceName)),
                contentDescription = service.serviceName,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = service.serviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
