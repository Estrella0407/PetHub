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
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.pethub.ui.StockManagement.StockManagementScreen
import com.example.pethub.ui.admin.AdminAppointmentDetail
import com.example.pethub.ui.admin.AdminHomeScreen
import com.example.pethub.ui.admin.ServiceManagementScreen
import com.example.pethub.ui.admin.AdminScannerScreen
import com.example.pethub.ui.admin.AdminViewAllAppointmentsScreen
import com.example.pethub.ui.admin.AdminAppointmentDetail
import com.example.pethub.ui.admin.MonthlySalesReportScreen
import com.example.pethub.ui.admin.ServiceManagementScreen
import com.example.pethub.ui.admin.ServiceUsageReportScreen
import com.example.pethub.ui.appointment.BookAppointmentScreen
import com.example.pethub.ui.auth.CompleteProfileScreen
import com.example.pethub.ui.auth.LoginScreen
import com.example.pethub.ui.auth.RegisterScreen
import com.example.pethub.ui.auth.RegisterViewModel
import com.example.pethub.ui.cart.CartScreen
import com.example.pethub.ui.home.HomeScreen
import com.example.pethub.ui.notifications.NotificationScreen
import com.example.pethub.ui.notifications.NotificationScreen
import com.example.pethub.ui.shop.ShopScreen
import com.example.pethub.ui.pet.AddPetScreen
import com.example.pethub.ui.pet.PetProfileScreen
import com.example.pethub.ui.profile.AllAppointmentsScreen
import com.example.pethub.ui.profile.AllOrdersScreen
import com.example.pethub.ui.profile.AppointmentDetailScreen
import com.example.pethub.ui.profile.OrderDetailScreen
import com.example.pethub.ui.auth.StartupRouterScreen
import com.example.pethub.ui.faq.BookingAppointmentScreenFAQ
import com.example.pethub.ui.faq.CancellationRescheduleFAQScreen
import com.example.pethub.ui.faq.FAQScreen
import com.example.pethub.ui.faq.PaymentFAQScreen
import com.example.pethub.ui.faq.PetInformationFAQScreen
import com.example.pethub.ui.faq.PetParentsFAQScreen
import com.example.pethub.ui.faq.PolicyFAQScreen
import com.example.pethub.ui.faq.SupportHelpFAQScreen
import com.example.pethub.ui.faq.PolicyFAQScreen
import com.example.pethub.ui.profile.ProfileScreen
import com.example.pethub.ui.profile.EditProfileScreen
import com.example.pethub.ui.service.ServiceDetailScreen
import com.example.pethub.ui.service.ServiceScreen
import com.example.pethub.ui.shop.ShopScreen
import com.example.pethub.ui.service.ServiceScreen
import com.example.pethub.ui.shop.ShopScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                onReturnClick = { navController.popBackStack() },
                onLoginSuccess = {
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
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("auth_flow")
                }
                val sharedVm: RegisterViewModel = hiltViewModel(parentEntry)
                RegisterScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToAdminHome = {
                        navController.navigate("admin_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToCompleteProfile = { navController.navigate("completeProfile") },
                    onNavigateToLogin = { navController.popBackStack() },
                    onReturnClick = { navController.popBackStack() },
                    viewModel = sharedVm
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
                onNavigateToNotification = { navController.navigate("notifications") },
                onServiceClick = { serviceName, serviceId -> // Receives both
                    // ALWAYS use the serviceId. We can pass the name as an optional parameter.
                    navController.navigate("serviceDetail/$serviceId?serviceName=$serviceName")
                }
            )
        }

        composable("notifications") {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("shop") {
            ShopScreen(
                onNavigateToCart = { navController.navigate("cart") },
                onNavigateToHome = { navController.navigate("home") },
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
                    navController.navigate("serviceDetail/$serviceId")
                }
            )
        }

        composable("cart") {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToShop = {
                    navController.navigate("shop") {
                        popUpTo("shop") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "serviceDetail/{serviceId}?serviceName={serviceName}",
            arguments = listOf(
                navArgument("serviceId") { type = NavType.StringType }, // ID is now mandatory
                navArgument("serviceName") { // Name is now optional
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            ServiceDetailScreen(
                onBackClick = { navController.popBackStack() },
                onProceedToBooking = { serviceId, petId, branchId ->
                    navController.navigate("appointments/$serviceId/$petId/$branchId")
                }
            )
        }

        composable(
            route = "appointments/{serviceId}/{petId}/{branchId}",
            arguments = listOf(
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("petId") { type = NavType.StringType },
                navArgument("branchId") { type = NavType.StringType }
            )
        ) {
            // Call the correct AppointmentScreen, the ViewModel will handle the arguments
            BookAppointmentScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookingSuccess = { navController.navigate("home") }
            )
        }


        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToService = { navController.navigate("services") },
                onNavigateToShop = { navController.navigate("shop") },
                onAddPetClick = { navController.navigate("addPet") },
                onFaqClick = { navController.navigate("faq") },
                onNavigateToPetProfile = { petId ->
                    navController.navigate("petProfile/$petId")
                },
                onAppointmentClick = { appointmentId ->
                    navController.navigate("appointmentDetail/$appointmentId")
                },
                onOrderClick = { orderId ->
                    navController.navigate("orderDetail/$orderId")
                },
                onNavigateToAllAppointments = {
                    navController.navigate("all_appointments")
                },
                onNavigateToAllOrders = {
                    navController.navigate("all_orders")
                },
                onEditProfileClick = {
                    navController.navigate("editProfile")
                }
            )
        }

        composable("editProfile") {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("all_appointments") {
            AllAppointmentsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAppointmentClick = { appointmentId ->
                    navController.navigate("appointmentDetail/$appointmentId")
                }
            )
        }

        composable(
            route = "appointmentDetail/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            AppointmentDetailScreen(
                appointmentId = appointmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("orderDetail/{orderId}") {
            OrderDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable("all_orders") {
            AllOrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate("orderDetail/$orderId")
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

        // Admin screens
        composable("admin_home") {
            AdminHomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToStocks = { navController.navigate("admin_stocks") },
                onNavigateToServices = { navController.navigate("admin_services") },
                onNavigateToScanner = { navController.navigate("admin_scanner") },
                onNavigateToAppointmentDetails = { appointmentId ->
                    navController.navigate("adminAppointmentDetail/${appointmentId}")
                },
                onViewAllClick = { navController.navigate("admin_view_all_appointments") },
                onNavigateToMonthlySalesReport = { navController.navigate("monthly_sales_report") },
                onNavigateToServiceUsageReport = { navController.navigate("service_usage_report") }
            )
        }

        composable("admin_stocks") {
            StockManagementScreen(
                onNavigateToAdminHome = { navController.navigate("admin_home") },
                onNavigateToAdminServices = { navController.navigate("admin_services") },
                onNavigateToAdminScanner = { navController.navigate("admin_scanner") },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("admin_home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "adminAppointmentDetail/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId")!!
            AdminAppointmentDetail(
                appointmentId = appointmentId,
                onAppointmentCanceled = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate("admin_home") },
                onNavigateToStocks = { navController.navigate("admin_stocks") },
                onNavigateToServices = { navController.navigate("admin_services") },
                onNavigateToScanner = { navController.navigate("admin_scanner") },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
        composable("admin_view_all_appointments") {
            AdminViewAllAppointmentsScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.popBackStack() },
                onNavigateToStocks = { navController.navigate("admin_stocks") },
                onNavigateToServices = { navController.navigate("admin_services") },
                onNavigateToScanner = { navController.navigate("admin_scanner") },
                onViewAppointmentClick = { appointmentId ->
                    navController.navigate("adminAppointmentDetail/${appointmentId}")
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

        composable("admin_scanner") {
            AdminScannerScreen(
                onQrScanned = { petId ->
                    navController.navigate("petProfile/$petId")
                }
            )
        }


        composable("faq") {
            FAQScreen(
                onNavigateBack = { navController.popBackStack() },
                onTopicClick = { topicId ->
                    when (topicId) {
                        "pet_parents" -> navController.navigate("petParentsFaq")
                        "pet_information" -> navController.navigate("petInformationFaq")
                        "booking_appointment" -> navController.navigate("bookingAppointmentFaq")
                        "cancellation_reschedule" -> navController.navigate("cancellationRescheduleFaq")
                        "payments" -> navController.navigate("paymentsFaq")
                        "policy" -> navController.navigate("policyFaq")
                        "support_help" -> navController.navigate("supportHelpFaq")
                    }
                }
            )
        }
        composable("petParentsFaq") {
            PetParentsFAQScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("petInformationFaq") {
            PetInformationFAQScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("bookingAppointmentFaq") {
            BookingAppointmentScreenFAQ(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("cancellationRescheduleFaq") {
            CancellationRescheduleFAQScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("paymentsFaq") {
            PaymentFAQScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("policyFaq") {
            PolicyFAQScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
        composable("supportHelpFaq") {
            SupportHelpFAQScreen(onNavigateBack = {
                navController.popBackStack()
            })
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