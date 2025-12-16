package com.example.pethub.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.navigation.AdminBottomNavigationBar
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamFair

@Composable
fun AppointmentDetail(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToStocks: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToScanner: () -> Unit,
    appointmentId: String,
    viewModel: AppointmentDetailViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                onLogoutClick = {
                    viewModel.logout(onLogoutSuccess = onNavigateToLogin)
                }
            )
        },
        bottomBar = {
            AdminBottomNavigationBar(
                modifier = Modifier,
                currentRoute = "admin_home",
                onNavigate = { route ->
                    when (route) {
                        "admin_home" -> onNavigateToHome()
                        "admin_stocks" -> onNavigateToStocks()
                        "admin_services" -> onNavigateToServices()
                        "admin_scanner" -> onNavigateToScanner()
                    }
                }
            )
        },
        containerColor = CreamBackground // Using your theme color
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "#${uiState.appointment?.appointmentId?.take(5) ?: ""}",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(24.dp)
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CreamFair
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(24.dp)
            ){
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ){
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ){
                        Text(
                            text= "Service"
                        )
                        Text(
                            text = "Owner's Contact"
                        )
                        Text(
                            text = "Date & Time"
                        )
                        Text(
                            text = "Branch"
                        )
                        Text(
                            text = "Pet Profile"
                        )
                    }
                    Spacer( modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .padding(horizontal = 2.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ){
                        Text(
                            text= "Pet Grooming"
                        )
                        Text(
                            text = "jenny@gmail.com"
                        )
                        Text(
                            text = "28 Sept 2025"
                        )
                        Text(
                            text = "PetHub KL"
                        )
                        Text(
                            text = "QR"
                        )
                    }
                }
            }
            Spacer (modifier = Modifier.height(16.dp))

            Text(
                text = "Reschedule",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .background(
                        color = CreamFair,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .align(Alignment.CenterHorizontally)
                    .clickable{}
                    .padding(16.dp)
                    .width(180.dp)
            )
            Spacer (modifier = Modifier.height(8.dp))
            Text(
                text = "Cancel Appointment",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .align(Alignment.CenterHorizontally)
                    .clickable{}
                    .padding(16.dp)
                    .width(180.dp)
            )
        }
    }
}

