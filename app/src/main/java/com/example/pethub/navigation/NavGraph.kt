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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.example.pethub.ui.admin.ServiceManagementScreen
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.ui.admin.AdminHomeScreen
import com.example.pethub.ui.auth.CompleteProfileScreen
import com.example.pethub.ui.auth.LoginScreen
import com.example.pethub.ui.auth.RegisterScreen
import com.example.pethub.ui.auth.RegisterViewModel
import com.example.pethub.ui.home.HomeScreen
import com.example.pethub.ui.notifications.NotificationScreen
import com.example.pethub.ui.shop.ShopScreen
import com.example.pethub.ui.pet.AddPetScreen
import com.example.pethub.ui.pet.PetProfileScreen
import com.example.pethub.ui.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    firebaseService: FirebaseService
) {
    val startDestination = if (firebaseService.isUserAuthenticated()) {
        "profile"
    } else {
//       "login"
        "admin_home"
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
                },
                onNotificationClick = { navController.navigate("notifications") }
            )
        }
        
        composable("notifications") {
            NotificationScreen(
                onNavigateUp = { navController.popBackStack() }
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

        // Admin screens
        composable("admin_home") {
            AdminHomeScreen(navController = navController)
        }
        composable("admin_stocks") { PlaceholderScreen("Stocks coming soon") }
        composable("admin_scanner") { PlaceholderScreen("Scanner coming soon") }

        composable("admin_services") {
            ServiceManagementScreen(navController = navController)
        }


        composable("profile") {
            ProfileScreen(
                //firebaseService = firebaseService,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToService = { navController.navigate("service") },
                onNavigateToShop = { navController.navigate("shop") },
                onAddPetClick = {navController.navigate("addPet")},
                onFaqClick = {navController.navigate("faq")},
                onNavigateToPetProfile = { petId ->
                    navController.navigate("petProfile/$petId")
                }
            )
        }

        composable("addPet") {
            AddPetScreen(
                onPetAdded = {
                    // Go back to profile after adding pet
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "petProfile/{petId}",
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) {
            PetProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
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
