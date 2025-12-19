package com.example.pethub.ui.admin

import android.util.Log
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.navigation.AdminBottomNavigationBar
import com.example.pethub.ui.theme.CreamBg
import com.example.pethub.ui.theme.CreamBackground
import com.example.pethub.ui.theme.CreamFair
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.example.pethub.ui.cart.PickerButton
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AdminAppointmentDetail(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToStocks: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToScanner: () -> Unit,
    appointmentId: String,
    onAppointmentCanceled: () -> Unit,
    viewModel: AdminAppointmentDetailViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()

    Box (modifier = Modifier.fillMaxSize()) {
        AppointmentDetailHero(
            onNavigateToHome = onNavigateToHome,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToStocks = onNavigateToStocks,
            onNavigateToServices = onNavigateToServices,
            onNavigateToScanner = onNavigateToScanner,
            appointmentId = appointmentId
        )

        if (uiState.showCancelOverlay){
            CancelAppointmentOverlay(
                onNoClicked = { viewModel.updateShowCancelOverlay(false) },
                onYesClicked = {
                    uiState.appointment?.let {
                        viewModel.removeAppointment(it)
                    }
                    viewModel.updateShowCancelOverlay(false)
                    onAppointmentCanceled()
                },
                closeOverlay = {viewModel.updateShowCancelOverlay(false)}
            )
        }

        if (uiState.showRescheduleOverlay) {
            RescheduleAppointmentOverlay(
                onDismiss = { viewModel.updateShowRescheduleOverlay(false) },
                onConfirm = { newDate ->
                    uiState.appointment?.appointmentId?.let { id ->
                        viewModel.rescheduleAppointment(id, newDate)
                    }
                    viewModel.updateShowRescheduleOverlay(false)
                }
            )
        }
    }
}

@Composable
fun AppointmentDetailHero(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToStocks: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToScanner: () -> Unit,
    appointmentId: String,
    viewModel: AdminAppointmentDetailViewModel = hiltViewModel()
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
                        .padding(horizontal = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ){
                        uiState.appointmentItem?.let { item ->
                            Text(text = "${item.serviceName}")
                        }

                        uiState.appointmentItem?.let { item ->
                            Text(text = "${uiState.appointmentItem?.owner?.custPhone}")
                        }

                        uiState.appointmentItem?.let { item ->
                            Text(text = "${item.dateTime}")
                        }

                        uiState.appointmentItem?.let { item ->
                            Text(text = "${uiState.appointmentItem?.locationName}")
                        }

                        uiState.appointmentItem?.let { item ->
                            Text(text = "Pet Profile")
                        }

                    }
                }
            }
            Spacer (modifier = Modifier.height(16.dp))

            Text(
                text = "Reschedule",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = Color(0xFFAC7F5E),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .align(Alignment.CenterHorizontally)
                    .clickable { viewModel.updateShowRescheduleOverlay(true) }
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
                    .clickable { viewModel.updateShowCancelOverlay(true) }
                    .padding(16.dp)
                    .width(180.dp)
            )
        }
    }
}

@Composable
fun CancelAppointmentOverlay (
    onNoClicked: () -> Unit,
    onYesClicked: () -> Unit,
    closeOverlay:() -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { closeOverlay() }
    ){
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(CreamBg, RoundedCornerShape(8.dp))
                .padding(20.dp)
                .clickable(enabled = false) {}
                .width(280.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "Confirm Cancel?",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer (modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)

            ){
                Text(
                    text = "No",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFAC7F5E),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onNoClicked() }
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 16.dp),
                    color = Color.White
                )

                Text(
                    text = "Yes",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFAC7F5E),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onYesClicked() }
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 16.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RescheduleAppointmentOverlay(
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(CreamBg, RoundedCornerShape(8.dp))
                .padding(24.dp)
                .clickable(enabled = false) {}
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reschedule",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PickerButton(
                    text = if (selectedDate.isEmpty()) "Date" else selectedDate,
                    icon = Icons.Default.CalendarToday,
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                )
                PickerButton(
                    text = if (selectedTime.isEmpty()) "Time" else selectedTime,
                    icon = Icons.Default.Schedule,
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFAC7F5E),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onDismiss() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White
                )

                Text(
                    text = "Confirm",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(
                            color = if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) Color(0xFFAC7F5E) else Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                            onConfirm(calendar.time)
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White
                )
            }
        }
    }
}