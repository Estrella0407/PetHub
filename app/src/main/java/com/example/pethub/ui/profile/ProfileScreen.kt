package com.example.pethub.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import java.util.*
import androidx.compose.material3.*
import com.example.pethub.ui.status.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToService: () -> Unit,
    onNavigateToShop: () -> Unit,
    onAddPetClick: () -> Unit,
    onFaqClick: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    onAppointmentClick: (String) -> Unit,
    onOrderClick: (String) -> Unit,
    onNavigateToAllAppointments: () -> Unit,
    onNavigateToAllOrders: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
            }
        }
    )

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
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
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                LoadingScreen()
            }
            is ProfileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProfileUiState.Success -> {
                ProfileContent(
                    modifier = Modifier.padding(paddingValues),
                    uiState = state,
                    onLogoutClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    onAddPetClick = onAddPetClick,
                    onFaqClick = onFaqClick,
                    onPetClick = { petId ->
                        onNavigateToPetProfile(petId)
                    },
                    onAppointmentClick = onAppointmentClick,
                    onOrderClick = onOrderClick,
                    onNavigateToAllAppointments = onNavigateToAllAppointments,
                    onNavigateToAllOrders = onNavigateToAllOrders,
                    onEditProfileClick = onEditProfileClick,
                    onPhotoClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
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
    onNavigateToAllOrders: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header Section ---
        // --- Header Section ---
        item {
            ProfileHeader(
                uiState = uiState,
                onFaqClick = onFaqClick,
                onEditProfileClick = onEditProfileClick,
                onPhotoClick = onPhotoClick
            )
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
                    uiState.appointments.forEach { appointmentItem ->
                        val title = appointmentItem.serviceName ?: "Service"
                        val dateStr = appointmentItem.dateTime ?: "No Date"

                        AppointmentCard(
                            title = title,
                            date = dateStr,
                            modifier = Modifier
                                .weight(1f) // Make cards share width
                                .clickable {
                                    onAppointmentClick(appointmentItem.id)
                                }
                        )
                    }
                    if (uiState.appointments.size < 2) {
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
                    uiState.orders.forEach { orderItem ->
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
                    if (uiState.orders.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // --- My Pet Section ---
        item {
            SectionHeader(title = "My Pets", onAddClick = onAddPetClick)
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.pets.isEmpty()) {
                Text("No pets added yet.", modifier = Modifier.padding(16.dp))
            } else {
                uiState.pets.forEach { pet ->
                    PetInfoCard(
                        pet = pet,
                        // Pass the onPetClick lambda to the card
                        onEditClick = { onPetClick(pet.petId) }
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
                colors = ButtonDefaults.buttonColors(containerColor = VibrantBrown)
            ) {
                Text(
                    text ="Log Out",
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    fontSize = 16.sp, color = Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

// THIS IS THE NEW COMPOSABLE
@Composable
fun PetInfoCard(
    pet: Pet,
    onEditClick: () -> Unit, // Changed parameter name for clarity
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                Text(pet.petName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBrown)
                Text("${pet.type} | ${pet.breed}", fontSize = 14.sp, color = Color.Gray)
            }
            // QR Code and Edit Button Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!pet.qrUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = pet.qrUrl,
                        contentDescription = "Pet QR Code",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "Edit Pet",
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.Underline,
                        color = MutedBrown
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MutedBrown
                    )
                }
            }
        }
    }
}


// Updated ProfileHeader with Image Upload capabilities
@Composable
fun ProfileHeader(
    uiState: ProfileUiState.Success,
    onFaqClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = uiState.customer?.profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, CreamDark, CircleShape)
                    .clickable(onClick = onPhotoClick), // Click to upload
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Default.Person),
            )

            // Show Progress Indicator if uploading
            if (uiState.isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = DarkBrown,
                    strokeWidth = 3.dp
                )
            }

            // Edit Icon overlay
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Change Image",
                tint = DarkBrown,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(20.dp)
                    .background(Color.White, CircleShape)
                    .padding(3.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = uiState.customer?.custName ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = uiState.customer?.custPhone ?: "", fontSize = 14.sp, color = Color.Gray)
            Text(text = uiState.customer?.custEmail ?: "", fontSize = 14.sp, color = Color.Gray)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onFaqClick) { Icon(Icons.Default.HelpOutline, contentDescription = "Help") }
            IconButton(onClick = onEditProfileClick) { Icon(Icons.Default.Edit, contentDescription = "Edit Profile") }
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
                color = DarkBrown,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "View Detail",
                style = MaterialTheme.typography.labelSmall,
                textDecoration = TextDecoration.Underline,
                color = MutedBrown,
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
    modifier: Modifier = Modifier
) {
    val statusColor = getStatusColor(status = status)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "#${orderNumber.take(5)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(text = "RM$price",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Status Chip
            Box(
                modifier = Modifier
                    .background(statusColor, shape = RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    color = DarkBrown.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
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
        onNavigateToAllOrders = {},
        onEditProfileClick = {},
        onPhotoClick = {}
    )
}
