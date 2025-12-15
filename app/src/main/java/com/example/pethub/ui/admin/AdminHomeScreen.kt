package com.example.pethub.ui.admin

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.navigation.AdminBottomNavigation
import com.example.pethub.ui.auth.LoginViewModel
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeScreenViewModel = hiltViewModel(),
    navController: NavController
){
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetHub") },
                actions = {
                    IconButton(onClick = { /* TODO: Logout */ }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = { AdminBottomNavigation(navController = navController) }
    ) {innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding (horizontal = 16.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ){
                Text(
                    text = "New Appointment",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text (
                    text = "view all",
                    fontSize = 16.sp
                )
            }
            NewAppointmentCard(uiState.appointments)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Report",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))


            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween){
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(80.dp)
                        .background(
                            Color(0xFFC6B7A3),
                            shape = RoundedCornerShape(6.dp))
                        .padding(end = 8.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFC6B7A3)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Monthly Sales Report",
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .align(Alignment.BottomEnd)
                            .clickable{}
                            .padding(horizontal=8.dp)
                    )

                }

                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(80.dp)
                        .background(
                            Color(0xFFC6B7A3),
                            shape = RoundedCornerShape(6.dp))
                        .padding(end = 8.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFC6B7A3)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Service Usage Report",
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .align(Alignment.BottomEnd)
                            .clickable{}
                            .padding(horizontal=8.dp)
                    )

                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreenContent(
    uiState: AdminHomeScreenUiState,
    navController: NavController
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PetHub") },
                actions = {
                    IconButton(onClick = { /* TODO: Logout */ }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = { AdminBottomNavigation(navController = navController) }
    ) {innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ){
                Text(
                    text = "New Appointment",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text (
                    text = "view all",
                    fontSize = 16.sp
                )
            }
            NewAppointmentCard(uiState.appointments)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Report",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))


            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween){
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(80.dp)
                        .background(
                            Color(0xFFC6B7A3),
                            shape = RoundedCornerShape(6.dp))
                        .padding(end = 8.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFC6B7A3)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Monthly Sales Report",
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .align(Alignment.BottomEnd)
                            .clickable{}
                            .padding(horizontal=8.dp)
                    )

                }

                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(80.dp)
                        .background(
                            Color(0xFFC6B7A3),
                            shape = RoundedCornerShape(6.dp))
                        .padding(end = 8.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFC6B7A3)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Service Usage Report",
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Text(
                        text = "View",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .align(Alignment.BottomEnd)
                            .clickable{}
                            .padding(horizontal=8.dp)
                    )

                }
            }
        }
    }
}

@Composable
fun NewAppointmentCard(
    appointments: List<Appointment>
){
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
    ){
        AppointmentList(appointments)
    }
}


@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun AdminHomeScreenPreview() {
    val navController = rememberNavController()
    val previewAppointments = listOf(
        Appointment(
            appointmentId = "A001",
            status = "pending",
            breed = "Golden Retriever",
            dateTime = Timestamp.now()
        ),
        Appointment(
            appointmentId = "A002",
            status = "confirmed",
            breed = "Shiba Inu",
            dateTime = Timestamp.now()
        )
    )
    AdminHomeScreenContent(
        uiState = AdminHomeScreenUiState(appointments = previewAppointments),
        navController = navController
    )
}
