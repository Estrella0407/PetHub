package com.example.pethub.ui.admin

import android.R.attr.text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pethub.data.model.Appointment
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import java.util.Date
import java.util.Locale

@Composable
fun AppointmentRow (
    appointment: Appointment
){
    val date = appointment.dateTime?.toDate()
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateText: String = formatter.format(date)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "#${appointment.appointmentId.take(5)}"
        )
        Text(
            text = dateText
        )
        TextButton(
            onClick = {}//View appointment in detail}
        ){
            Text("view")
        }
    }
}