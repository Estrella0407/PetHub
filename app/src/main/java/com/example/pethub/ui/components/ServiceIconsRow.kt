package com.example.pethub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pethub.R
import com.example.pethub.data.model.ServiceItem
import com.example.pethub.utils.getServiceIcon


/**
 * Service Icons Row
 */
@Composable
fun ServiceIconsRow(
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ){
        Image(
            painter = painterResource(R.drawable.daycare_nobg),
            contentDescription = "Daycare",
            modifier = Modifier.size(50.dp).alpha(0.8f)
        )
        Image(
            painter = painterResource(R.drawable.boarding_nobg),
            contentDescription = "Boarding",
            modifier = Modifier.size(50.dp).alpha(0.8f)
        )
        Image(
            painter = painterResource(R.drawable.grooming_nobg),
            contentDescription = "Grooming",
            modifier = Modifier.size(50.dp).alpha(0.8f)
        )
        Image(
            painter = painterResource(R.drawable.training_nobg),
            contentDescription = "Training",
            modifier = Modifier.size(50.dp).alpha(0.8f)
        )
        Image(
            painter = painterResource(R.drawable.walking_nobg),
            contentDescription = "Walking",
            modifier = Modifier.size(50.dp).alpha(0.8f)
        )
    }
}

