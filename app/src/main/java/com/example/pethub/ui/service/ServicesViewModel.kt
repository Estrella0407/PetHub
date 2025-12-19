package com.example.pethub.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Service
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceUiState(
    val service: Service,
    val isAvailable: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _branchId = MutableStateFlow("")

    val services: StateFlow<List<ServiceUiState>> = _branchId.flatMapLatest { branchId ->
        if (branchId.isEmpty()) {
            flowOf(emptyList())
        } else {
            val servicesFlow = serviceRepository.listenToServices()
            val branchServicesFlow = serviceRepository.listenToAllBranchServices(branchId)
            combine(servicesFlow, branchServicesFlow) { services, branchServices ->
                services.map { service ->
                    val isAvailable = branchServices.find { it.serviceId == service.serviceId }?.availability ?: false
                    ServiceUiState(service, isAvailable)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _branchId.value = authRepository.getCurrentUserId() ?: ""
        }
    }

    fun onAvailabilityChanged(serviceId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            if (_branchId.value.isNotEmpty()) {
                serviceRepository.setBranchServiceAvailability(_branchId.value, serviceId, isAvailable)
            }
        }
    }
}
