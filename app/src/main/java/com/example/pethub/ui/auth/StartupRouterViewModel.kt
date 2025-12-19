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

            // 2. Check Role
            val isAdmin = authRepository.isAdmin()
            if (isAdmin) {
                _route.value = "admin_home"
            } else {
                _route.value = "home"
            }
        }
    }
}
