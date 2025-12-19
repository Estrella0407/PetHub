package com.example.pethub.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.ui.auth.AddressSection
import com.example.pethub.ui.auth.BasicInfoSection
import com.example.pethub.ui.auth.CompleteButton
import com.example.pethub.ui.status.LoadingScreen
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamDark
import com.example.pethub.ui.theme.DarkBrown
import com.example.pethub.ui.theme.VibrantBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = DarkBrown) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground,
                    titleContentColor = CreamDark,
                    navigationIconContentColor = CreamDark
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.name.isEmpty()) {
            LoadingScreen()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BasicInfoSection(
                        username = uiState.name,
                        phone = uiState.phone,
                        onUsernameChange = viewModel::onNameChange,
                        onPhoneChange = viewModel::onPhoneChange
                    )
                }

                item {
                    AddressSection(
                        houseNo = uiState.houseNo,
                        streetName = uiState.streetName,
                        city = uiState.city,
                        postcode = uiState.postcode,
                        state = uiState.state,
                        onHouseNoChange = viewModel::onHouseNoChange,
                        onStreetNameChange = viewModel::onStreetNameChange,
                        onCityChange = viewModel::onCityChange,
                        onPostcodeChange = viewModel::onPostcodeChange,
                        onStateChange = viewModel::onStateChange
                    )
                }

                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::saveProfile,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantBrown)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
