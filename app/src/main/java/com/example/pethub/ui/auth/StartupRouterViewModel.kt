package com.example.pethub.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartupRouterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _route = MutableStateFlow<String?>(null)
    val route = _route.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // 1. Check if user is authenticated at all
            if (!authRepository.isUserAuthenticated()) {
                _route.value = "login"
                return@launch
            }

            // 2. Check "Remember Me" preference (matching MainActivity logic)
            val prefs = context.getSharedPreferences(Constants.NAME, Context.MODE_PRIVATE)
            val remember = prefs.getBoolean(Constants.REMEMBER_ME, false)

            if (!remember) {
                authRepository.signOut()
                _route.value = "login"
                return@launch
            }

            // 3. User is logged in and "Remember Me" is true. Now check role.
            val isAdmin = authRepository.isAdmin()
            if (isAdmin) {
                _route.value = "admin_home"
            } else {
                _route.value = "home"
            }
        }
    }
}
