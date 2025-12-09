package com.example.pethub.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pethub.ui.auth.LoginScreen
import com.example.pethub.ui.auth.RegisterScreen
import com.example.pethub.ui.home.HomeScreen
import com.example.pethub.ui.shop.ShopScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = { 
                    // Pop login from backstack so back button exits app
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToServices = { navController.navigate("services") },
                onNavigateToBookings = { navController.navigate("bookings") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToShop = { navController.navigate("shop") },
                onServiceClick = { serviceId ->
                    navController.navigate("service/$serviceId")
                },
                onBookingClick = { bookingId ->
                    navController.navigate("booking/$bookingId")
                }
            )
        }
        
        composable("shop") {
            ShopScreen(
                onNavigateToCart = { navController.navigate("cart") },
                onNavigateToHome = { 
                    navController.navigate("home") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToServices = { 
                    navController.navigate("services") {
                         popUpTo("home") { saveState = true }
                         launchSingleTop = true
                         restoreState = true
                    }
                },
                onNavigateToProfile = { 
                    navController.navigate("profile") {
                         popUpTo("home") { saveState = true }
                         launchSingleTop = true
                         restoreState = true
                    }
                }
            )
        }

        composable("services") { PlaceholderScreen("Services coming soon") }
        composable("bookings") { PlaceholderScreen("Bookings coming soon") }
        composable("profile") { PlaceholderScreen("Profile coming soon") }
        composable("service/{serviceId}") { PlaceholderScreen("Service details coming soon") }
        composable("booking/{bookingId}") { PlaceholderScreen("Booking details coming soon") }
        composable("cart") { PlaceholderScreen("Cart coming soon") }
    }
}

@Composable
private fun PlaceholderScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
