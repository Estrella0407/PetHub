package com.example.pethub.ui.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import com.example.pethub.data.model.Appointment


@Composable
fun AppointmentList(
    appointments: List<Appointment>
){
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        items(appointments) {appointment ->
            AppointmentRow(appointment)
        }
    }
}