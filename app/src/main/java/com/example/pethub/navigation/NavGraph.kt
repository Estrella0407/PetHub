package com.example.pethub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.ui.auth.LoginScreen
import com.example.pethub.ui.auth.RegisterScreen
import com.example.pethub.ui.home.HomeScreen
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
                onNavigateToService = { navController.navigate("service") },
                onNavigateToShop = { navController.navigate("shop") },
                onNavigateToProfile = { navController.navigate("profile") },
                onServiceClick = { serviceId ->
                    navController.navigate("service/$serviceId")
                }
            )
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

