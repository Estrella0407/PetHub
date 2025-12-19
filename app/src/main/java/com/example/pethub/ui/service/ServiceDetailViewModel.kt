package com.example.pethub.ui.service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.Service
import com.example.pethub.data.repository.BranchRepository
import com.example.pethub.data.repository.PetRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceDetailUiState(
    val service: Service? = null,
    val serviceTypes: List<Service> = emptyList(),
    val availableBranches: List<Branch> = emptyList(),
    val userPets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val selectedBranch: Branch? = null,
    val selectedServiceType: Service? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val branchRepository: BranchRepository,
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    private val serviceName: String =
        savedStateHandle.get<String>("serviceName")!!

    private val preselectedServiceId: String? =
        savedStateHandle.get<String>("serviceId")

    init {
        loadInitialData(serviceName, preselectedServiceId)
    }

    private fun loadInitialData(categoryName: String, preselectedId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load service types
                val serviceTypes = serviceRepository
                    .getServicesByCategory(categoryName)
                    .getOrElse { throw it }

                // Load pets
                val pets = petRepository
                    .getCurrentUserPets()
                    .getOrElse { throw it }

                // 3Preselect service if provided
                val preselectedService = preselectedId?.let { id ->
                    serviceTypes.find { it.serviceId == id }
                }

                _uiState.update {
                    it.copy(
                        service = preselectedService ?: serviceTypes.firstOrNull(),
                        serviceTypes = serviceTypes,
                        userPets = pets,
                        selectedServiceType = preselectedService,
                        isLoading = false
                    )
                }

                // 4Load branches ONLY if a service is preselected
                preselectedService?.let {
                    loadBranches(it.serviceId)
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onPetSelected(pet: Pet) {
        _uiState.update { it.copy(selectedPet = pet) }
    }

    fun onBranchSelected(branch: Branch) {
        _uiState.update { it.copy(selectedBranch = branch) }
    }

    fun onServiceTypeSelected(serviceType: Service) {
        _uiState.update {
            it.copy(
                selectedServiceType = serviceType,
                service = serviceType,
                selectedBranch = null,
                availableBranches = emptyList()
            )
        }

        loadBranches(serviceType.serviceId)
    }

    private fun loadBranches(serviceId: String) {
        viewModelScope.launch {
            branchRepository
                .getAvailableBranchesForService(serviceId)
                .onSuccess { branches ->
                    _uiState.update {
                        it.copy(availableBranches = branches)
                    }
                }
        }
    }
}

