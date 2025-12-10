package com.example.pethub.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.pethub.ui.admin.ServiceManagementScreen
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.ui.auth.CompleteProfileScreen
import com.example.pethub.ui.auth.LoginScreen
import com.example.pethub.ui.auth.RegisterScreen
import com.example.pethub.ui.auth.RegisterViewModel
import com.example.pethub.ui.home.HomeScreen
import com.example.pethub.ui.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    firebaseService: FirebaseService
) {
    val startDestination = if (firebaseService.isUserAuthenticated()) {
        "home"
    } else {
       "login"
    }
    NavHost(navController = navController, startDestination = startDestination) {
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = { 
                    // Pop login from backstack so back button exits app
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToAdmin = {
                    navController.navigate("admin_services") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") { backStackEntry ->
            RegisterScreen(
                onRegisterSuccessOld = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onReturnClick = {navController.popBackStack()},
                onNavigateToCompleteProfile={navController.navigate("completeProfile")},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("completeProfile"){backStackEntry ->
            val parentEntry = remember {
                navController.getBackStackEntry("register")
            }
            val sharedVm: RegisterViewModel = hiltViewModel(parentEntry)
            CompleteProfileScreen(
                onProfileCompleted = {navController.navigate("home")},
                viewModel = sharedVm
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToService = { navController.navigate("service") },
                onNavigateToShop = { navController.navigate("shop") },
                onNavigateToProfile = { navController.navigate("profile") },
                onServiceClick = { serviceId ->
                    navController.navigate("service/$serviceId")
                }
            )
        }

        composable("services") { PlaceholderScreen("Services coming soon") }
        composable("bookings") { PlaceholderScreen("Bookings coming soon") }
        composable("profile") { PlaceholderScreen("Profile coming soon") }
        composable("service/{serviceId}") { PlaceholderScreen("Service details coming soon") }
        composable("booking/{bookingId}") { PlaceholderScreen("Booking details coming soon") }

        // Admin screens
        composable("admin_home") { PlaceholderScreen("Admin Home coming soon") }
        composable("admin_stocks") { PlaceholderScreen("Stocks coming soon") }
        composable("admin_services") { ServiceManagementScreen(navController = navController) }
        composable("admin_scanner") { PlaceholderScreen("Scanner coming soon") }
    }
}
//        composable("service") {
//            ServiceScreen(
//                onNavigateUp = { navController.popBackStack() },
//                onServiceClick = { serviceId ->
//                    navController.navigate("service/$serviceId")
//                }
//            )
//        }

        composable("profile") {
            ProfileScreen(
                onFaqClick = {},
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToService = { navController.navigate("service") },
                onNavigateToShop = { navController.navigate("shop") }
            )
        }
    }
}

