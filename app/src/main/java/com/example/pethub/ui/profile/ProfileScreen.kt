package com.example.pethub.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.R
import com.example.pethub.data.model.Pet // 👈 Make sure you have this Pet data class
import com.example.pethub.ui.components.BottomNavigationBar
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.*
import com.example.pethub.util.calculateAge
import java.text.SimpleDateFormat
import java.util.*

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
    // 1. ADD a new parameter for navigating to the pet's profile
    onNavigateToPetProfile: (petId: String) -> Unit
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
                        "service" -> onNavigateToService()
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
                    // 2. PASS the navigation function down
                    onPetClick = { petId ->
                        onNavigateToPetProfile(petId)
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
    // 3. RECEIVE the pet click listener
    onPetClick: (petId: String) -> Unit
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
            SectionHeader(title = "Appointments", onViewAllClick = { /* TODO */ })
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AppointmentCard(title = "Grooming", date = "17 Dec 2025, 14:00", modifier = Modifier.weight(1f))
                AppointmentCard(title = "Walking", date = "18 Dec 2025, 14:00", modifier = Modifier.weight(1f))
            }
        }

        // --- My Orders Section (using hardcoded data for now) ---
        item {
            SectionHeader(title = "My Orders", viewAllText = "Past Orders", onViewAllClick = { /* TODO */ })
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OrderCard(orderNumber = "#012", date = "17 Dec 2025", price = "RM 35.00", status = "Unpaid", statusColor = Color.Red, modifier = Modifier.weight(1f))
                OrderCard(orderNumber = "#011", date = "17 Nov 2025", price = "RM 35.00", status = "Picked Up", statusColor = Color.Green, modifier = Modifier.weight(1f))
            }
        }

        // --- My Pet Section ---
        item {
            SectionHeader(title = "My Pet", onAddClick = onAddPetClick)
            Spacer(modifier = Modifier.height(8.dp))
            // 4. CHECK for pets and display them
            if (uiState.pets.isEmpty()) {
                Text("No pets added yet.", modifier = Modifier.padding(16.dp))
            } else {
                // This will create a card for each pet
                uiState.pets.forEach { pet ->
                    PetInfoCard(
                        pet = pet, // Pass the whole pet object
                        modifier = Modifier.clickable {
                            // 5. CALL the navigation function when a pet is clicked
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
            Text(text = uiState.customer?.custName ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CreamDark)
            Text(text = uiState.customer?.custPhone ?: "", fontSize = 14.sp, color = Color.Gray)
            Text(text = uiState.customer?.custEmail ?: "", fontSize = 14.sp, color = Color.Gray)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onFaqClick) { Icon(Icons.Default.HelpOutline, contentDescription = "Help") }
            IconButton(onClick = { /* TODO: Navigate to Edit Profile */ }) { Icon(Icons.Default.Edit, contentDescription = "Edit Profile") }
        }
    }
}

// No changes needed for SectionHeader, AppointmentCard, OrderCard

@Composable
fun SectionHeader(title: String, viewAllText: String = "View All", onViewAllClick: (() -> Unit)? = null, onAddClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CreamDark)
        Spacer(modifier = Modifier.weight(1f))
        onViewAllClick?.let {
            Text(
                text = viewAllText,
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

@Composable
fun AppointmentCard(title: String, date: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = date, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "View Detail",
                textDecoration = TextDecoration.Underline,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun OrderCard(orderNumber: String, date: String, price: String, status: String, statusColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CreamLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Order $orderNumber", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = date, fontSize = 12.sp, color = Color.Gray)
            Text(text = price, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("View Detail", textDecoration = TextDecoration.Underline, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// 6. SIMPLIFY PetInfoCard to only require a Pet object and a Modifier
@Composable
fun PetInfoCard(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    val age = calculateAge(pet.dateOfBirth)

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
                modifier = Modifier.size(60.dp).clip(CircleShape),
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
        onPetClick = {}
    )
}
