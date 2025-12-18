package com.example.pethub.ui.auth

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.utils.Constants
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
        _uiState.value =
            _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, errorMessage = null)
    }

    fun onAddressUpdated(
        houseNo: String = "",
        streetName: String = "",
        city: String = "",
        postcode: String = "",
        state: String = ""
    ) {
        val address = houseNo + ", " + streetName + ", " + city + ", " + postcode +
                ", " + state
        _uiState.value = _uiState.value.copy(address = address, errorMessage = null)
    }

    fun register() {
        val state = _uiState.value

        // Validation
        if (state.email.isBlank() || state.password.isBlank()) {
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

        // If valid
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            //val result = authRepository.register(state.email, state.password)//, state.username) Dont register yet

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSuccess = true,
                successMessage = "Registration successful!"
            )

        }
    }

    fun completeProfile() {

        val state = _uiState.value
        // Validation
        if (state.username.isBlank() || state.phone.isBlank() || state.address.isBlank()) {
            _uiState.value = state.copy(errorMessage = "All fields are required")
            return
        }
        
        // Smart Dispatch: If user is already authenticated (e.g. via Google), use the Google flow
        if (authRepository.isUserAuthenticated()) {
            completeGoogleProfile()
            return
        }

        // If valid and not authenticated, proceed with full registration (Auth + Firestore)
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val result = authRepository.register(state.email, state.username, state.password,state.phone,state.address)//, state.username)

            if(result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    completed = true,
                    successMessage = "Registration successful!"
                )
            }
            else{
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }

    }

    fun completeGoogleProfile() {
        val state = _uiState.value
        // Validation
        if (state.username.isBlank() || state.phone.isBlank() || state.address.isBlank()) {
            _uiState.value = state.copy(errorMessage = "All fields are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            
            val result = authRepository.createCustomerProfile(
                username = state.username, 
                phone = state.phone, 
                address = state.address
            )

            if(result.isSuccess) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    completed = true,
                    successMessage = "Profile completed successfully!"
                )
            } else {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Profile completion failed"
                )
            }
        }
    }

    fun checkGoogleUserStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.signInWithGoogle()

            result.onSuccess { signInResult ->
                when (signInResult) {
                    is com.example.pethub.data.repository.GoogleSignInResult.ExistingUser -> {
                        val authData = signInResult.authResult
                        val role = if (authData.isAdmin) "admin" else "customer"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            loggedInUserRole = role
                        )
                    }
                    is com.example.pethub.data.repository.GoogleSignInResult.NewUser -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isNewGoogleUser = true
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Google Sign-In failed"
                )
            }
        }
    }

    fun onNewUserHandled() {
        _uiState.value = _uiState.value.copy(isNewGoogleUser = false)
    }

    fun onLoginHandled() {
        _uiState.value = _uiState.value.copy(isLoginSuccessful = false)
    }

    fun onRememberMeChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun saveRememberMe(context: Context, remember: Boolean) {
        val prefs = context.getSharedPreferences(Constants.NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(Constants.REMEMBER_ME, remember) }
    }
}


data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val address: String = "",
    val phone: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val isNewGoogleUser: Boolean = false,
    val loggedInUserRole: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val completed: Boolean = false,
    val rememberMe: Boolean = false
)
