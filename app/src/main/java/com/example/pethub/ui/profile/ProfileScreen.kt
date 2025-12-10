package com.example.pethub.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.pethub.ui.components.*
import com.example.pethub.ui.status.ErrorScreen
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.DarkBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onFaqClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToService: () -> Unit,
    onNavigateToShop: () -> Unit
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
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onFaqClick) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Change Account",
                            tint = DarkBrown
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "profile",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "service" -> onNavigateToService()
                        "shop" -> onNavigateToShop()
                        // "profile" -> do nothing or scroll to top
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                LoadingScreen()
            }
            is ProfileUiState.Success -> {
                ProfileContent(
                    modifier = Modifier.padding(paddingValues),
                    uiState = state,
                    onImageClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onLogoutClick = {
                        viewModel.logout() // Clear Firebase/DataStore
                        onLogout()         // Navigate to Login Screen
                    }
                )
            }
            is ProfileUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { /* Call your retry logic in ViewModel */ }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState.Success,
    onImageClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ProfileHeader(
                uiState = uiState,
                onImageClick = onImageClick
            )
        }
        item {
            // Logout Button
            TextButton(onClick = onLogoutClick) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileHeader(
    uiState: ProfileUiState.Success,
    onImageClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 24.dp)) {
        AsyncImage(
            model = uiState.customer?.profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(onClick = onImageClick),
            contentScale = ContentScale.Crop,
            fallback = rememberVectorPainter(Icons.Default.Person)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Change Picture",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        if (uiState.isUploading) {
            CircularProgressIndicator(
                progress = uiState.uploadProgress,
                modifier = Modifier.size(120.dp)
            )
        }
    }
}

@Composable
fun ProfileInfoCard(uiState: ProfileUiState) {
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
}
