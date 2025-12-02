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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = null)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun register() {
        val state = _uiState.value
        
        // Validation
        if (state.username.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "All fields are required")
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Passwords do not match")
            return
        }

        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            
            val result = authRepository.register(state.email, state.password, state.username)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Registration successful!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }
}

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
