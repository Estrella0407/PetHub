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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceDetailUiState(
    val mainService: Service? = null,
    val pets: List<Pet> = emptyList(),
    val relatedServices: List<Service> = emptyList(),
    // Renamed for clarity to reflect its filtered nature
    val availableBranches: List<Branch> = emptyList(),
    val selectedPet: Pet? = null,
    val selectedServiceType: Service? = null,
    val selectedBranch: Branch? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val serviceRepository: ServiceRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val serviceId: String = savedStateHandle.get<String>("serviceId")!!

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch the main service to get its category, image, etc.
                val mainService = serviceRepository.getServiceById(serviceId).getOrNull()

                // Fetch pets and related services
                val petsList = petRepository.getPetsForCurrentUser().first() // Use first() to get the current list from the Flow
                val relatedServicesResult = serviceRepository.getServicesByCategory(mainService?.serviceName ?: "")

                _uiState.update {
                    val relatedServices = relatedServicesResult.getOrElse { emptyList() }
                    it.copy(
                        isLoading = false,
                        mainService = mainService,
                        pets = petsList,
                        relatedServices = relatedServices,
                        // Pre-select the service type based on the initial serviceId
                        selectedServiceType = relatedServices.find { s -> s.serviceId == serviceId }
                    )
                }

                // After setting the initial state, fetch the branches for the pre-selected service
                _uiState.value.selectedServiceType?.let { fetchAvailableBranches(it.serviceId) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load data: ${e.message}") }
            }
        }
    }

    // This function is called every time a new service type is selected
    private fun fetchAvailableBranches(serviceId: String) {
        viewModelScope.launch {
            val branchesResult = branchRepository.getBranchesOfferingService(serviceId)
            _uiState.update {
                it.copy(
                    availableBranches = branchesResult.getOrElse { emptyList() },
                    // Reset branch selection when the service type changes
                    selectedBranch = null
                )
            }
        }
    }

    fun onPetSelected(pet: Pet) {
        _uiState.update { it.copy(selectedPet = pet) }
    }

    fun onServiceTypeSelected(service: Service) {
        _uiState.update { it.copy(selectedServiceType = service) }
        // When a new service type is selected, re-fetch the branches that offer it.
        fetchAvailableBranches(service.serviceId)
    }

    fun onBranchSelected(branch: Branch) {
        _uiState.update { it.copy(selectedBranch = branch) }
    }
}
