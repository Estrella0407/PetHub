package com.example.pethub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.pethub.data.remote.FirebaseService
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.navigation.NavGraph
import com.example.pethub.ui.theme.PetHubTheme
import com.example.pethub.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var firebaseService: FirebaseService

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences(Constants.NAME, MODE_PRIVATE)
        val remember = prefs.getBoolean(Constants.REMEMBER_ME, false)

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
                    // Create a state to hold the decision
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    // Run the check asynchronously when the app starts
                    LaunchedEffect(Unit) {
                        val role = authRepository.getUserRole()
                        startDestination = when (role) {
                            "admin" -> "admin_home"
                            "customer" -> "home"
                            else -> "login"
                        }
                    }

                    // Show Loading until we know where to go, otherwise show NavGraph
                    if (startDestination == null) {
                        // Simple Loading Screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }
}