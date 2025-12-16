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
    // Dummy list of services based on the image
    val services = listOf(
        Service(serviceId = "grooming", serviceName = "Grooming", description = "This service ....", imageUrl = ""),
        Service(serviceId = "boarding", serviceName = "Boarding", description = "This service ....", imageUrl = ""),
        Service(serviceId = "walking", serviceName = "Walking", description = "This service ....", imageUrl = ""),
        Service(serviceId = "daycare", serviceName = "Daycare", description = "This service ....", imageUrl = ""),
        Service(serviceId = "training", serviceName = "Training", description = "This service ....", imageUrl = "")
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
            items(services) { service ->
                ServiceCard(
                    service = service,
                    onClick = { onServiceClick(service.serviceId) }
                )
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service, onClick: () -> Unit) {
    val iconRes = when (service.serviceName) {
        "Grooming" -> R.drawable.grooming_nobg
        "Boarding" -> R.drawable.boarding_nobg
        "Walking" -> R.drawable.walking_nobg
        "Daycare" -> R.drawable.daycare_nobg
        "Training" -> R.drawable.training_nobg
        else -> R.drawable.pethub_rvbg // A default icon
    }

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
                painter = painterResource(id = iconRes),
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
