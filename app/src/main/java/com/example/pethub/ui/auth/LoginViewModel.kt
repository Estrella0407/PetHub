package com.example.pethub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = "")
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = "")
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            val result = authRepository.signIn(email, password)
            
            if (result.isSuccess) {
                val isAdmin = authRepository.isAdmin()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = true,
                    loggedInUserRole = if (isAdmin) "admin" else "customer"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }
    
    fun loginAsGuest() {
         _uiState.value = _uiState.value.copy(
            isLoginSuccessful = true,
            loggedInUserRole = "guest"
        )
    }

    fun onLoginHandled() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val loggedInUserRole: String? = null,
    val errorMessage: String = ""
)
