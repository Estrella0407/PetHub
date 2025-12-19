package com.example.pethub.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.R

@Composable
fun StartupRouterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    viewModel: StartupRouterViewModel = hiltViewModel()
) {
    val route by viewModel.route.collectAsState()

    LaunchedEffect(route) {
        when (route) {
            "login" -> onNavigateToLogin()
            "home" -> onNavigateToHome()
            "admin_home" -> onNavigateToAdminHome()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_nobg),
                contentDescription = "PetHub Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
