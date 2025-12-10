package com.example.pethub.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Customer
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()


    init {
        loadCustomerData()
    }

    private fun loadCustomerData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                customerRepository.listenToCurrentCustomer().collect { customer ->
                    // Transition to Success state
                    _uiState.value = ProfileUiState.Success(customer = customer)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Failed to load data")
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            // check current state just for the initial check
            if (_uiState.value is ProfileUiState.Success) {
                try {
                    // Start Uploading
                    _uiState.update { state ->
                        if (state is ProfileUiState.Success) {
                            state.copy(isUploading = true, uploadProgress = 0f)
                        } else state
                    }

                    customerRepository.uploadProfileImage(uri) { progress ->
                        // Update Progress safely
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                state.copy(uploadProgress = progress)
                            } else state
                        }
                    }.onSuccess { imageUrl ->
                        // Update Firestore
                        customerRepository.updateProfileImageUrl(imageUrl)

                        // Stop uploading.
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                state.copy(isUploading = false)
                            } else state
                        }
                    }.onFailure { e ->
                        // Handle failure safely
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                ProfileUiState.Error("Upload failed: ${e.message}", customer = state.customer)
                            } else state
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception safely
                    _uiState.update { state ->
                        if (state is ProfileUiState.Success) {
                            ProfileUiState.Error("Upload failed", customer = state.customer)
                        } else state
                    }
                }
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

sealed class ProfileUiState(open val customer: Customer? = null) {
    // Initial loading state when screen opens
    data object Loading : ProfileUiState()

    // Main state when data is loaded. Can also be uploading.
    data class Success(
        override val customer: Customer?,
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f
    ) : ProfileUiState(customer)

    // Error state, but can still show old customer data if we have it.
    data class Error(
        val message: String,
        override val customer: Customer? = null // Pass the old data to show behind the error
    ) : ProfileUiState(customer)
}
