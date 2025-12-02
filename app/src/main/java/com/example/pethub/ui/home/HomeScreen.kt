package com.example.pethub.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * Main Home Screen
 * Jetpack Compose automatically handles orientation changes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToServices: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onServiceClick: (String) -> Unit,
    onBookingClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val recommendedServices by viewModel.recommendedServices.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                userName = userName,
                onProfileClick = onNavigateToProfile,
                onNotificationClick = { /* Navigate to notifications */ }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is HomeUiState.Loading -> {
                LoadingScreen()
            }
            is HomeUiState.Success -> {
                HomeContent(
                    modifier = Modifier.padding(paddingValues),
                    userName = userName,
                    selectedPetName = selectedPet?.petName ?: "your pet",
                    recommendedServices = recommendedServices,
                    onServiceClick = onServiceClick,
                    onBookingClick = onBookingClick,
                    onViewAllServices = onNavigateToServices,
                    onViewAllBookings = onNavigateToBookings
                )
            }
            is HomeUiState.Error -> {
                ErrorScreen(
                    message = (uiState as HomeUiState.Error).message,
                    onRetry = { viewModel.loadData() }
                )
            }
        }
    }
}

/**
 * Top App Bar with greeting and actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    userName: String,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "PetHub",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        Badge { Text("3") }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFF9E6) // Cream color like in design
        )
    )
}

/**
 * Main content of the home screen
 * Jetpack Compose automatically adapts to different screen sizes!
 */
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    userName: String,
    selectedPetName: String,
    recommendedServices: List<ServiceItem>,
    onServiceClick: (String) -> Unit,
    onBookingClick: (String) -> Unit,
    onViewAllServices: () -> Unit,
    onViewAllBookings: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9E6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Greeting Section with cute pets illustration
        item {
            GreetingSection(userName = userName)
        }

        // Business Hours Card
        item {
            BusinessHoursCard()
        }

        // Pet Selection / Quick Action Section
        item {
            QuickServiceSection(
                userName = userName,
                petName = selectedPetName,
                services = recommendedServices.take(3),
                onServiceClick = onServiceClick
            )
        }


        // Recommended Services Section
        item {
            SectionHeader(
                title = "All Services",
                actionText = "View All",
                onActionClick = onViewAllServices
            )
        }

        // Services Grid (shows first 6)
        items(recommendedServices.chunked(2)) { rowServices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowServices.forEach { service ->
                    ServiceCard(
                        service = service,
                        onClick = { onServiceClick(service.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Add empty space if odd number of services
                if (rowServices.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Greeting section with cute pet illustrations (like in design)
 */
@Composable
fun GreetingSection(userName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cute pets illustration placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFFFF0D6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFFFF9E7B)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Hello, $userName! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "How can we help your pets today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Business Hours Card (like in design)
 */
@Composable
fun BusinessHoursCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF4E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFFF9E7B)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Business Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Monday - Friday: 10:00 - 20:00",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Saturday - Sunday: 10:00 - 18:00",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Quick Service Section - Shows services for specific pet (like in design)
 */
@Composable
fun QuickServiceSection(
    userName: String,
    petName: String,
    services: List<ServiceItem>,
    onServiceClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF4E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hello, $userName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Check out these services for $petName:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                services.forEach { service ->
                    QuickServiceCard(
                        service = service,
                        onClick = { onServiceClick(service.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Quick service card with icon (Grooming, Daycare, Training like in design)
 */
@Composable
fun QuickServiceCard(
    service: ServiceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Service icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFFFF0D6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getServiceIcon(service.category),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFFFF9E7B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Section header with "View All" button
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C)
        )

        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                color = Color(0xFFFF9E7B)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFFF9E7B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Horizontal scrolling row of upcoming bookings (Now Appointments)
 */
@Composable
fun UpcomingAppointmentsRow(
    appointments: List<AppointmentItem>, // Changed from BookingItem
    onAppointmentClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appointments) { appointment ->
            AppointmentCard(
                appointment = appointment,
                onClick = { onAppointmentClick(appointment.id) }
            )
        }
    }
}

/**
 * Appointment card showing details
 */
@Composable
fun AppointmentCard(
    appointment: AppointmentItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )

                StatusBadge(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            BookingDetailRow(
                icon = Icons.Default.Pets,
                text = appointment.petName
            )

            Spacer(modifier = Modifier.height(8.dp))

            BookingDetailRow(
                icon = Icons.Default.CalendarToday,
                text = appointment.dateTime
            )

            Spacer(modifier = Modifier.height(8.dp))

            BookingDetailRow(
                icon = Icons.Default.LocationOn,
                text = appointment.locationName
            )
        }
    }
}

/**
 * Booking detail row with icon
 */
@Composable
fun BookingDetailRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

/**
 * Status badge for appointments
 */
@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFFFFF4E0) to Color(0xFFFF9E7B)
        "confirmed" -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
        "completed" -> Color(0xFFE3F2FD) to Color(0xFF2196F3)
        else -> Color.LightGray to Color.DarkGray
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Service card for grid display
 */
@Composable
fun ServiceCard(
    service: ServiceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Service image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFFFF0D6))
            ) {
                if (service.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = service.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = getServiceIcon(service.category),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = Color(0xFFFF9E7B)
                    )
                }
            }

            // Service details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", service.rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${service.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9E7B)
                )
            }
        }
    }
}

/**
 * Get icon for service category
 */
fun getServiceIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "grooming" -> Icons.Default.ContentCut
        "veterinary" -> Icons.Default.MedicalServices
        "boarding" -> Icons.Default.Hotel
        "training" -> Icons.Default.School
        "walking" -> Icons.Default.DirectionsWalk
        "daycare" -> Icons.Default.ChildCare
        "sitting" -> Icons.Default.Weekend
        else -> Icons.Default.Pets
    }
}

/**
 * Loading screen
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFFF9E7B)
        )
    }
}

/**
 * Error screen with retry button
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9E7B)
            )
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}