package com.example.pethub.ui.service

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pethub.R
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.Service
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.PetHubTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    viewModel: ServiceDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onProceedToBooking: (serviceId: String, petId: String, branchId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.mainService?.type ?: "Service Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            ServiceDetailContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onPetSelected = viewModel::onPetSelected,
                onServiceTypeSelected = viewModel::onServiceTypeSelected,
                onBranchSelected = viewModel::onBranchSelected,
                onBookAppointmentClick = {
                    val selectedService = uiState.selectedServiceType
                    val selectedPet = uiState.selectedPet
                    val selectedBranch = uiState.selectedBranch
                    if (selectedService != null && selectedPet != null && selectedBranch != null) {
                        onProceedToBooking(selectedService.serviceId, selectedPet.petId, selectedBranch.branchId)
                    }
                }
            )
        }
    }
}

@Composable
fun ServiceDetailContent(
    modifier: Modifier = Modifier,
    uiState: ServiceDetailUiState,
    onPetSelected: (Pet) -> Unit,
    onServiceTypeSelected: (Service) -> Unit,
    onBranchSelected: (Branch) -> Unit,
    onBookAppointmentClick: () -> Unit
) {
    val iconRes = when (uiState.mainService?.serviceName) {
        "Grooming" -> R.drawable.grooming_nobg
        "Boarding" -> R.drawable.boarding_nobg
        "Walking" -> R.drawable.walking_nobg
        "Daycare" -> R.drawable.daycare_nobg
        "Training" -> R.drawable.training_nobg
        else -> R.drawable.pethub_rvbg // A default icon
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = uiState.mainService?.imageUrl,
                contentDescription = uiState.mainService?.type,
                placeholder = painterResource(id = R.drawable.grooming_nobg),
                error = painterResource(id = R.drawable.grooming_nobg),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = uiState.mainService?.description ?: "Details about this service.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Form Section ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CustomDropdown(
                label = "For Who?",
                items = uiState.pets,
                selectedItem = uiState.selectedPet,
                onItemSelected = onPetSelected,
                itemToString = { it.petName },
                placeholder = "Choose Pet"
            )

            CustomDropdown(
                label = "Service Type",
                items = uiState.relatedServices,
                selectedItem = uiState.selectedServiceType,
                onItemSelected = onServiceTypeSelected,
                itemToString = { it.type },
                placeholder = "Choose Service Type"
            )

            CustomDropdown(
                label = "Branch",
                items = uiState.availableBranches,
                selectedItem = uiState.selectedBranch,
                onItemSelected = onBranchSelected,
                itemToString = { it.branchName },
                placeholder = "Choose Branch"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Book Button ---
        Button(
            onClick = onBookAppointmentClick,
            enabled = uiState.selectedPet != null && uiState.selectedServiceType != null && uiState.selectedBranch != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CreamDark)
        ) {
            Text("Book Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun <T> CustomDropdown(
    label: String,
    placeholder: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CreamDark)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedItem?.let(itemToString) ?: placeholder,
                    modifier = Modifier.weight(1f),
                    color = if (selectedItem == null) Color.Gray else LocalContentColor.current
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemToString(item)) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
