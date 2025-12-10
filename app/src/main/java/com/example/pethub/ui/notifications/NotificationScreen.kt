package com.example.pethub.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pethub.data.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet.")
            }
        } else {
            LazyColumn(contentPadding = it) {
                items(notifications) { notification ->
                    NotificationItem(notification = notification, onNotificationClicked = {
                        // Mark as read when clicked
                        viewModel.markAsRead(notification.id)
                    })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationClicked: () -> Unit
) {
    val backgroundColor = if (notification.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNotificationClicked)
            .background(backgroundColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!notification.isRead) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = "Unread",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(8.dp).padding(end = 8.dp)
            )
        } else {
             Spacer(modifier = Modifier.width(16.dp)) // Maintain alignment with read items
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = notification.title, fontWeight = FontWeight.Bold)
            Text(text = notification.message, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(notification.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
