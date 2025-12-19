package com.example.pethub.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.R
import com.example.pethub.data.model.Pet
import com.example.pethub.navigation.BottomNavigationBar
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.*
import com.example.pethub.util.calculateAge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToService: () -> Unit,
    onNavigateToShop: () -> Unit,
    onAddPetClick: () -> Unit,
    onAppointmentClick: (appointmentId: String) -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    onFaqClick: () -> Unit,
    onNavigateToPetProfile: (petId: String) -> Unit,
    onNavigateToAllAppointments: () -> Unit,
    onNavigateToAllOrders: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CreamBackground,
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "profile",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "services" -> onNavigateToService()
                        "shop" -> onNavigateToShop()
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> LoadingScreen()
            is ProfileUiState.Error -> ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadCustomerData() }
            )
            is ProfileUiState.Success -> {
                ProfileContent(
                    modifier = Modifier.padding(padding),
                    uiState = state,
                    onLogoutClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    onAddPetClick = onAddPetClick,
                    onFaqClick = onFaqClick,
                    // PASS the navigation function down
                    onPetClick = { petId ->
                        onNavigateToPetProfile(petId)
                    },
                    onAppointmentClick = onAppointmentClick,
                    onOrderClick = onOrderClick,
                    onNavigateToAllAppointments = onNavigateToAllAppointments,
                    onNavigateToAllOrders = onNavigateToAllOrders
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState.Success,
    onLogoutClick: () -> Unit,
    onAddPetClick: () -> Unit,
    onFaqClick: () -> Unit,
    onPetClick: (petId: String) -> Unit,
    onAppointmentClick: (appointmentId: String) -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    onNavigateToAllAppointments: () -> Unit,
    onNavigateToAllOrders: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header Section ---
        item {
            ProfileHeader(uiState = uiState, onFaqClick = onFaqClick)
        }

        // --- Appointments Section
        item {
            SectionHeader(title = "Appointments", onViewAllClick = onNavigateToAllAppointments)

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.appointments.isEmpty()) {
                Text("No upcoming appointments.", modifier = Modifier.padding(16.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between cards
                ) {
                    // Use .forEach to iterate through the list
                    uiState.appointments.forEach { appointmentItem ->

                        // Extract the strings from the object
                        // Ensure your Appointment model has these fields
                        val title = appointmentItem.serviceName ?: "Service"
                        val dateStr = appointmentItem.dateTime ?: "No Date"

                        AppointmentCard(
                            title = title,
                            date = dateStr,
                            modifier = Modifier
                                .fillMaxWidth() // Make cards take full width
                                .padding(bottom = 8.dp) // Add space between cards
                                .clickable {
                                    onAppointmentClick(appointmentItem.id)
                                }
                        )
                    }

                    if (uiState.appointments.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- My Orders Section ---
        item {
            SectionHeader(title = "Orders", onPastOrdersClick = onNavigateToAllOrders)

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.orders.isEmpty()) {
                Text("No orders.", modifier = Modifier.padding(16.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Iterate through OrderItem list
                    uiState.orders.forEach { orderItem ->

                        // Use fields from OrderItem
                        val pickupDate: Date? = orderItem.pickupDateTime.toDate()
                        val formattedDate = if (pickupDate != null) {
                            SimpleDateFormat(
                                "dd MMM | hh:mm a",
                                Locale.getDefault()
                            ).format(pickupDate)
                        } else {
                            "No Pickup Time"
                        }

                        OrderCard(
                            orderNumber = orderItem.id,
                            date = formattedDate,
                            price = orderItem.totalPrice.toString(),
                            status = orderItem.status,
                            modifier = Modifier
                                .weight(1f) // Equal width
                                .clickable { onOrderClick(orderItem.id) }
                        )
                    }

                    // Spacer for alignment if only 1 item
                    if (uiState.orders.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- My Pet Section ---
        item {
            SectionHeader(title = "My Pets", onAddClick = onAddPetClick)
            Spacer(modifier = Modifier.height(8.dp))
            // CHECK for pets and display them
            if (uiState.pets.isEmpty()) {
                Text("No pets added yet.", modifier = Modifier.padding(16.dp))
            } else {
                // This will create a card for each pet
                uiState.pets.forEach { pet ->
                    PetInfoCard(
                        pet = pet, // Pass the whole pet object
                        modifier = Modifier.clickable {
                            // CALL the navigation function when a pet is clicked
                            onPetClick(pet.petId)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // --- Logout Button ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CreamDark)
            ) {
                Text("Log Out", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp)
            }
        }
    }
}

// No changes needed for ProfileHeader
@Composable
fun ProfileHeader(uiState: ProfileUiState.Success, onFaqClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = uiState.customer?.profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, CreamDark, CircleShape),
            contentScale = ContentScale.Crop,
            fallback = rememberVectorPainter(Icons.Default.Person),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = uiState.customer?.custName ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = uiState.customer?.custPhone ?: "", fontSize = 14.sp, color = Color.Gray)
            Text(text = uiState.customer?.custEmail ?: "", fontSize = 14.sp, color = Color.Gray)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onFaqClick) { Icon(Icons.Default.HelpOutline, contentDescription = "Help") }
            IconButton(onClick = { /* TODO: Navigate to Edit Profile */ }) { Icon(Icons.Default.Edit, contentDescription = "Edit Profile") }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    viewAllText: String = "View All",
    onViewAllClick: (() -> Unit)? = null,
    onAddClick: (() -> Unit)? = null,
    onPastOrdersClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onViewAllClick?.let {
                Text(
                    text = viewAllText,
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = it)
                )
            }
            onPastOrdersClick?.let {
                Text(
                    text = "Past Orders",
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = it)
                )
            }
            onAddClick?.let {
                Text(
                    text = "+ Add Pet",
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = it),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AppointmentCard(
    title: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CreamDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Date with Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2, // Allow date/time to wrap if needed
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "View Detail" indicator
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelSmall,
                textDecoration = TextDecoration.Underline,
                color = CreamDark,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun OrderCard(
    orderNumber: String,
    date: String,
    price: String,
    status: String,
    modifier: Modifier = Modifier) {

    val statusColor = when (status) {
        "Pending" -> Color.Yellow
        "Confirmed" -> Color.Green
        "Cancelled" -> Color.Red
        else -> Color.Yellow
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order $orderNumber",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(text = price,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = status,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PetInfoCard(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    val age = calculateAge(pet.dateOfBirth?.time)

    Card(
        modifier = modifier.fillMaxWidth(), // Apply the clickable modifier here
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pet.imageUrl,
                contentDescription = pet.petName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Default.Person)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = pet.petName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Breed: ${pet.breed}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Age: ${age?.toString() ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Image(
                    painter = painterResource(id = R.drawable.qr),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Edit",
                    textDecoration = TextDecoration.Underline,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
        }
    }
}

// No changes needed for Preview
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ProfileScreenPreview() {
    ProfileContent(
        uiState = ProfileUiState.Success(customer = null, pets = listOf(Pet(petId = "1", petName = "Yeemi"))),
        onLogoutClick = {},
        onAddPetClick = {},
        onFaqClick = {},
        onPetClick = {},
        onAppointmentClick = {},
        onOrderClick = {},
        onNavigateToAllAppointments = {},
        onNavigateToAllOrders = {}
    )
}
