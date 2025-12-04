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
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState()) //_uistate holds a set of values, it is initialized with LoginUiState(), it can be modified
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow() //This is a read only version of _uistate, the ui will read the states from uistate
    // Why do we need a _uistate and uistate? This is for protection, we don't want the ui to be able to change the uistate, hence we create 2 version of uistate.
    // One can be modified by the viewmodel, and another one which can only be read by the ui.

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
            val result = authRepository.signIn(email, password)  // concern with firebase
            
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

    fun onRememberMeChanged(value: Boolean){
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun signInWithGoogle(context:Context){

    }

}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val loggedInUserRole: String? = null,
    val errorMessage: String = "",
    val rememberMe: Boolean = false
)
