package com.example.pethub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.pethub.data.model.Notification
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.repository.NotificationRepository
import com.example.pethub.navigation.NavGraph
import com.example.pethub.utils.SharedPrefs
import com.example.pethub.ui.theme.PetHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var firebaseService: FirebaseService

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private var notificationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        // Setup channel and start listening
        createNotificationChannel()
        observeNotifications()

        val prefs = getSharedPreferences(SharedPrefs.NAME, MODE_PRIVATE)
        val remember = prefs.getBoolean(SharedPrefs.REMEMBER_ME, false)

        if (!remember) {
            firebaseService.signOut()
            // or FirebaseAuth.getInstance().signOut()
        }
        setContent {
            PetHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        firebaseService = firebaseService
                    )
                }
            }
        }
    }

    private fun observeNotifications() {
        lifecycleScope.launch {
            // Restart listener whenever user logs in/out
            firebaseService.observeAuthState().collectLatest { user ->
                notificationJob?.cancel()
                
                if (user != null) {
                    notificationJob = launch {
                        var isFirstLoad = true
                        // Listen to the Firestore collection
                        notificationRepository.getUserNotifications().collect { notifications ->
                            if (!isFirstLoad && notifications.isNotEmpty()) {
                                // Get the most recent notification
                                val latest = notifications.firstOrNull()
                                // If it's unread, show a popup
                                if (latest != null && !latest.isRead) {
                                    showSystemNotification(latest)
                                }
                            }
                            isFirstLoad = false
                        }
                    }
                }
            }
        }
    }

    private fun showSystemNotification(notification: Notification) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        
        // Navigate to notification screen (optional handling in NavGraph needed to deep link)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "pethub_notifications"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Use your app logo for the notification icon
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_pethub_nobg) // Changed to your logo
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "pethub_notifications"
            val channelName = "PetHub Notifications"
            val channelDescription = "Notifications for appointments and updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
