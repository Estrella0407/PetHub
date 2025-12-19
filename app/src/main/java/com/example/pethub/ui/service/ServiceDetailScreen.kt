package com.example.pethub.ui.service

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.Service
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamLight
import com.example.pethub.ui.theme.DarkBrown
import com.example.pethub.ui.theme.MutedBrown

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
                title = { Text(uiState.service?.serviceName ?: "Service Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkBrown) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                onBookClick = {
                    val s = uiState.selectedServiceType
                    val p = uiState.selectedPet
                    val b = uiState.selectedBranch
                    if (s != null && p != null && b != null) {
                        onProceedToBooking(s.serviceId, p.petId, b.branchId)
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
    onBookClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header with Icon and Dynamic Description
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRes = when (uiState.service?.serviceName?.lowercase()) {
                "grooming" -> R.drawable.grooming_nobg
                "boarding" -> R.drawable.boarding_nobg
                "walking" -> R.drawable.walking_nobg
                "daycare" -> R.drawable.daycare_nobg
                "training" -> R.drawable.training_nobg
                else -> R.drawable.pethub_rvbg
            }
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = uiState.selectedServiceType?.description ?: uiState.service?.description ?: "Details about this service.",
                fontSize = 14.sp,
                color = MutedBrown,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Dropdowns styled like Appointment Screen
        CustomDropdown(
            label = "For Who?",
            items = uiState.userPets,
            selectedItem = uiState.selectedPet,
            onItemSelected = onPetSelected,
            itemToString = { it.petName },
            placeholder = "Choose Pet"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomDropdown(
            label = "Service Type",
            items = uiState.serviceTypes,
            selectedItem = uiState.selectedServiceType,
            onItemSelected = onServiceTypeSelected,
            itemToString = { it.type },
            placeholder = "Choose Service Type"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomDropdown(
            label = "Branch",
            items = uiState.availableBranches,
            selectedItem = uiState.selectedBranch,
            onItemSelected = onBranchSelected,
            itemToString = { it.branchName },
            placeholder = "Choose Branch"
        )

        if (uiState.selectedServiceType != null && uiState.availableBranches.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No branches available for this service",
                color = DarkBrown,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. Book Button
        Button(
            onClick = onBookClick,
            enabled = uiState.selectedPet != null && uiState.selectedServiceType != null && uiState.selectedBranch != null,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2DCC8))
        ) {
            Text("Book Appointment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkBrown)
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
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkBrown)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CreamLight)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedItem?.let(itemToString) ?: placeholder,
                    modifier = Modifier.weight(1f),
                    color = if (selectedItem == null) Color.Gray else Color.Black
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f).background(CreamLight)
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
