package com.example.pethub.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import com.example.pethub.data.model.ServiceItem
import com.example.pethub.navigation.BottomNavigationBar
import com.example.pethub.ui.components.*
import com.example.pethub.ui.status.*
import com.example.pethub.utils.getServiceIcon

/**
 * Main Home Screen
 * Jetpack Compose automatically handles orientation changes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToService: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToShop: () -> Unit,
    onServiceClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val recommendedServices by viewModel.recommendedServices.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    val branches by viewModel.branches.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                onNotificationClick = {/*navigate to notification*/ },
                onChangeAccTypeClick = { /* Change account type */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                modifier = Modifier.fillMaxWidth(),
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "home" -> { /* Stay */
                        }

                        "services" -> onNavigateToService()
                        "shop" -> onNavigateToShop()
                        "profile" -> onNavigateToProfile()
                    }
                }
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
                    branches = branches,
                    allPets = pets,
                    onPetSelected = viewModel::selectPet,
                    onServiceClick = onServiceClick,
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
 * Top App Bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onChangeAccTypeClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    TopAppBar(
        title = {},
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MutedBrown,
                    modifier = Modifier
                        .background(
                            color = CreamDark,
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )
            }
            IconButton(onClick = onChangeAccTypeClick) {
                Icon(
                    imageVector = Icons.Default.ManageAccounts,
                    contentDescription = "Change Account",
                    tint = MutedBrown,
                    modifier = Modifier
                        .background(
                            color = CreamDark,
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )
            }
        }
    )
}

/**
 * Main content of the home screen
 */
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    userName: String,
    selectedPetName: String,
    recommendedServices: List<ServiceItem>,
    branches: List<Branch>,
    allPets: List<Pet>,
    onPetSelected: (Pet) -> Unit,
    onServiceClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PetHub Logo
        item {
            Image(
                painter = painterResource(id = R.drawable.pethub_logo_rvbg),
                contentDescription = "PetHub Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .size(200.dp)
            )
        }

        // Business Hours Card
        item {
            BusinessHoursCard()
        }

        // Recommended Services Section Header
        item {
            RecommendedServiceSection(
                userName = userName,
                petName = selectedPetName,
                services = recommendedServices.take(3),
                allPets = allPets,
                onPetSelected = onPetSelected,
                onServiceClick = onServiceClick
            )
        }

        // Shop Details Section Header
        if (branches.isNotEmpty()) {
             item {
                 // Shop Details / Branches
                 ShopDetailsSection(
                     branches = branches,
                     modifier = Modifier.fillMaxWidth()
                 )
            }
        }

        // Bottom padding for navigation bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


/**
 * Business Hours Card
 */
@Composable
fun BusinessHoursCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.opening_nobg),
                contentDescription = "PetHub Business Hours",
                modifier = Modifier.size(100.dp)
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
 * Recommended Service Section - Shows services for specific pet
 */
@Composable
fun RecommendedServiceSection(
    userName: String,
    petName: String,
    services: List<ServiceItem>,
    allPets: List<Pet>,
    onPetSelected: (Pet) -> Unit,
    onServiceClick: (String) -> Unit
) {
    var showPetSelection by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hello, $userName👋!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkBrown
            )

            // If showPetSelection is true, display the dropdown
            PetSelectionDropdown(
                pets = allPets,
                selectedPetName = petName,
                onPetSelected = { pet ->
                    onPetSelected(pet) // Call the ViewModel
                    showPetSelection = false // Close the dropdown
                },
                onDismiss = { showPetSelection = false }, // Close if clicked outside
                expanded = showPetSelection // Pass the state
            )

            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader(
                title = "Check out these services for $petName:",
                actionText = "Change Pet",
                onActionClick = { showPetSelection = true } // Open the dropdown
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                services.forEach { service ->
                    RecommendedServiceCard(
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
 * Recommended service card with icon
 */
@Composable
fun RecommendedServiceCard(
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
            containerColor = CreamLight
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
                    .size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getServiceIcon(service.serviceName)),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.type,
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
 * Section header
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
            style = MaterialTheme.typography.titleMedium,
            color = NeutralBrown
        )

        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                color = MutedBrown
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MutedBrown,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Pet selection
 */
@Composable
fun PetSelectionDropdown(
    pets: List<Pet>,
    selectedPetName: String,
    onPetSelected: (Pet) -> Unit,
    onDismiss: () -> Unit,
    expanded: Boolean = true
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        pets.forEach { pet ->
            DropdownMenuItem(
                text = { Text(pet.petName) },
                onClick = { onPetSelected(pet) },
                leadingIcon = {
                    if (pet.petName == selectedPetName) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected Pet"
                        )
                    }
                }
            )
        }
    }
}

/**
 * Shop Details Section
 */
@Composable
fun ShopDetailsSection(
    branches: List<Branch>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CreamLight,
            contentColor = DarkBrown
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.pethub_rvbg),
                contentDescription = "PetHub Logo",
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pet Services | Pet Care | Pawsome Food",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )

            // Details info row
            branches.forEachIndexed { index, branch ->
                InfoParagraphs(
                    title = branch.branchName,
                    subtitle = branch.branchAddress,
                    text = branch.branchPhone,
                    modifier = Modifier.fillMaxWidth()
                )
                if (index < branches.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ServiceIconsRow()
        }
    }
}


@Composable
fun InfoParagraphs(
    title: String,
    subtitle: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}
