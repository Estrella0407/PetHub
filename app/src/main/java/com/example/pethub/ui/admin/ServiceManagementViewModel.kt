package com.example.pethub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Service
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to hold the final, combined information for the UI
data class ServiceManagementItem(
    val serviceId: String,
    val name: String,
    val isEnabled: Boolean
)

// Sealed interface to represent the entire screen state
sealed interface ServiceManagementUiState {
    object Loading : ServiceManagementUiState
    data class Success(val services: List<ServiceManagementItem>) : ServiceManagementUiState
    data class Error(val message: String) : ServiceManagementUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ServiceManagementViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // 1. THIS IS THE STATIC LIST OF SERVICES. It is defined only once and will not repeat.
    private val staticServices = flowOf(
        listOf(
            Service(serviceId = "grooming", serviceName = "Grooming"),
            Service(serviceId = "boarding", serviceName = "Boarding"),
            Service(serviceId = "walking", serviceName = "Walking"),
            Service(serviceId = "daycare", serviceName = "DayCare"),
            Service(serviceId = "training", serviceName = "Training")
        )
    )

    // A flow that emits the current user's ID (branchId) or null if logged out
    private val branchIdFlow: Flow<String?> = authRepository.observeAuthState().map { it?.uid }

    // The main state flow for the UI.
    val uiState: StateFlow<ServiceManagementUiState> = branchIdFlow.flatMapLatest { branchId ->
        if (branchId == null) {
            flowOf(ServiceManagementUiState.Loading)
        } else {
            // 2. COMBINE the static list with the dynamic availability from Firebase.
            combine(
                staticServices,
                serviceRepository.listenToAllBranchServices(branchId)
            ) { allServices, branchAvailabilities ->
                val availabilityMap = branchAvailabilities.associateBy { it.serviceId }
                val uiItems = allServices.map {
                    ServiceManagementItem(
                        serviceId = it.serviceId,
                        name = it.serviceName,
                        isEnabled = availabilityMap[it.serviceId]?.availability ?: false
                    )
                }
                ServiceManagementUiState.Success(uiItems)
            }
        }
    }.catch { e ->
        emit(ServiceManagementUiState.Error(e.message ?: "An unknown error occurred"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceManagementUiState.Loading
    )

    /**
     * Updates the availability for a specific service in Firebase.
     * Passes both serviceId and serviceName to match the database structure.
     */
    fun updateServiceAvailability(serviceId: String, serviceName: String, isAvailable: Boolean) {
        viewModelScope.launch {
            val currentBranchId = branchIdFlow.first()
            if (currentBranchId != null) {
                serviceRepository.setBranchServiceAvailability(
                    branchId = currentBranchId,
                    serviceId = serviceId,
                    serviceName = serviceName,
                    isAvailable = isAvailable
                )
            }
        }
    }

    /**
     * Logs the current user out.
     */
    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onLogoutSuccess()
        }
    }
}
