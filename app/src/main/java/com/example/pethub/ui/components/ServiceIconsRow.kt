package com.example.pethub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pethub.data.model.ServiceItem
import com.example.pethub.utils.getServiceIcon


/**
 * Service Icons Row
 */
@Composable
fun ServiceIconsRow(
    services: List<ServiceItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        services.forEach { service ->
            // Use the helper function to get the drawable ID
            val iconResId = getServiceIcon(service.category)

            Image(
                painter = painterResource(id = iconResId),
                contentDescription = service.category,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

