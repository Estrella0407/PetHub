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
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.pethub.ui.service.ServiceScreen
import com.example.pethub.ui.admin.ServiceManagementScreen
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.ui.admin.AdminHomeScreen
import com.example.pethub.ui.admin.AdminDashboardScreen
import com.example.pethub.ui.admin.ServiceManagementScreen
import com.example.pethub.ui.admin.AdminScannerScreen
import com.example.pethub.ui.admin.AdminViewAllAppointmentsScreen
import com.example.pethub.ui.admin.AppointmentDetail
import com.example.pethub.ui.appointnment.AppointmentScreen
import com.example.pethub.ui.admin.MonthlySalesReportScreen
import com.example.pethub.ui.admin.ServiceUsageReportScreen
import com.example.pethub.ui.auth.CompleteProfileScreen
import com.example.pethub.ui.auth.LoginScreen
import com.example.pethub.ui.auth.RegisterScreen
import com.example.pethub.ui.auth.RegisterViewModel
import com.example.pethub.ui.cart.CartScreen
import com.example.pethub.ui.home.HomeScreen
import com.example.pethub.ui.notifications.NotificationScreen
import com.example.pethub.ui.shop.ShopScreen
import com.example.pethub.ui.pet.AddPetScreen
import com.example.pethub.ui.pet.PetProfileScreen
import com.example.pethub.ui.profile.ProfileScreen
import com.example.pethub.ui.service.ServiceScreen
import com.example.pethub.ui.shop.ShopScreen

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
                onReturnClick = {navController.popBackStack()},
                onLoginSuccess = {
                    // Pop login from backstack so back button exits app
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToAdminHome = {
                    navController.navigate("admin_home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToCompleteProfile = {
                    // Navigate to the completeProfile route in the auth_flow graph
                    navController.navigate("completeProfile")
                }
            )
        }

        navigation(startDestination = "register", route = "auth_flow") {

            composable("register") { backStackEntry ->
                // Get the ViewModel scoped to the "auth_flow" graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("auth_flow")
                }
                val sharedVm: RegisterViewModel = hiltViewModel(parentEntry)

                RegisterScreen(
                    viewModel = sharedVm, // Pass the shared VM
                    onRegisterSuccessOld = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onReturnClick = { navController.popBackStack() },
                    onNavigateToCompleteProfile = { navController.navigate("completeProfile") },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable("completeProfile") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("auth_flow")
                }
                val sharedVm: RegisterViewModel = hiltViewModel(parentEntry)

                CompleteProfileScreen(
                    onProfileCompleted = { navController.navigate("home") },
                    viewModel = sharedVm
                )
            }
        }

        composable("home") {
            HomeScreen(
                onNavigateToService = { navController.navigate("services") },
                onNavigateToShop = { navController.navigate("shop") },
                onNavigateToProfile = { navController.navigate("profile") },
                onServiceClick = { serviceId ->
                    navController.navigate("service/$serviceId")
                }
            )
        }
        
        composable("shop") {
            ShopScreen(
                onNavigateToCart = { navController.navigate("cart") },
                onNavigateToHome = { navController.navigate("home")},
                onNavigateToServices = { navController.navigate("services") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("services") {
            ServiceScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToShop = { navController.navigate("shop") },
                onNavigateToProfile = { navController.navigate("profile") },
                onServiceClick = { serviceId ->
                    navController.navigate("appointment/$serviceId")
                }
            )
        }
        composable("bookings") { PlaceholderScreen("Bookings coming soon") }
        composable("profile") { PlaceholderScreen("Profile coming soon") }
        composable("service/{serviceId}") { PlaceholderScreen("Service details coming soon") }
        composable("booking/{bookingId}") { PlaceholderScreen("Booking details coming soon") }
        composable("cart") {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "appointment/{serviceId}",
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) {
            AppointmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin screens
        composable("admin_home") {
            AdminDashboardScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToStocks = { navController.navigate("admin_stocks") },
                onNavigateToServices = { navController.navigate("admin_services") },
                onNavigateToScanner = { navController.navigate("admin_scanner") },
                onNavigateToAppointmentDetails = {appointmentId->
                    navController.navigate("appointmentDetail/${appointmentId}")
                },
                onViewAllClick = {navController.navigate("admin_view_all_appointments")},
                onNavigateToMonthlySalesReport = { navController.navigate("monthly_sales_report") },
                onNavigateToServiceUsageReport = { navController.navigate("service_usage_report") }
            )
        }
        composable("admin_stocks") { PlaceholderScreen("Stocks coming soon") }
        composable("admin_scanner") {
            AdminScannerScreen { qr ->
                navController.navigate("petProfile/${qr}")
            }
        }
        composable(
            route = "appointmentDetail/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ){backStackEntry ->

            val appointmentId =
                backStackEntry.arguments?.getString("appointmentId")!!

            AppointmentDetail(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = {navController.popBackStack()},
                onNavigateToStocks = { navController.navigate("admin_stocks") },
                onNavigateToServices = { navController.navigate("admin_services") },
                onNavigateToScanner = { navController.navigate("admin_scanner") },
                appointmentId = appointmentId,
                onAppointmentCanceled = {navController.navigate("admin_home")}
            )
        }
        composable("admin_view_all_appointments"){
            AdminViewAllAppointmentsScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = {navController.popBackStack()},
                onNavigateToStocks = { navController.navigate("admin_stocks") },
                onNavigateToServices = { navController.navigate("admin_services") },
                onNavigateToScanner = { navController.navigate("admin_scanner") },
                onViewAppointmentClick = {appointmentId->
                    navController.navigate("appointmentDetail/${appointmentId}")
                }
            )
        }
        composable("admin_services") {
            ServiceManagementScreen(
                onNavigateToAdminHome = { navController.navigate("admin_home") },
                onNavigateToAdminStocks = { navController.navigate("admin_stocks") },
                onNavigateToAdminScanner = { navController.navigate("admin_scanner") },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("monthly_sales_report") {
            MonthlySalesReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("service_usage_report") {
            ServiceUsageReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                //firebaseService = firebaseService,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToService = { navController.navigate("services") },
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

        composable("faq") { PlaceholderScreen("FAQ coming soon") }
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

//        composable("service") {
//            ServiceScreen(
//                onNavigateUp = { navController.popBackStack() },
//                onServiceClick = { serviceId ->
//                    navController.navigate("service/$serviceId")
//                }
//            )
//        }
