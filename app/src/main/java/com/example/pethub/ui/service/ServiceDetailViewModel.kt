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
    val availableBranches: List<Branch> = emptyList(),
    val selectedPet: Pet? = null,
    val selectedServiceType: Service? = null,
    val selectedBranch: Branch? = null,
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
    val uiState = _uiState.asStateFlow()

    private val serviceId: String = savedStateHandle.get<String>("serviceId")!!

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Step 1: Fetch pets first (needed regardless)
                val petsList = petRepository.getPetsForCurrentUser().first()

                // Step 2: Try to fetch the service by ID
                val mainServiceResult = serviceRepository.getServiceById(serviceId)
                val mainService = mainServiceResult.getOrNull()

                // Step 3: Determine the service category name
                // If we successfully got the service, use its name
                // Otherwise, use the serviceId as the category name (fallback for navigation from ServiceScreen)
                val serviceCategoryName = mainService?.serviceName ?: serviceId.capitalize()

                // Step 4: Fetch related services using the category name
                val relatedServicesResult = serviceRepository.getServicesByCategory(serviceCategoryName)
                val relatedServices = relatedServicesResult.getOrElse { emptyList() }

                // Step 5: Determine which service to use as the main service
                // Priority:
                // 1. Service found by exact ID match from related services
                // 2. Service found by getServiceById
                // 3. First service in related services
                val actualMainService = relatedServices.find { it.serviceId == serviceId }
                    ?: mainService
                    ?: relatedServices.firstOrNull()

                if (actualMainService == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Service not found."
                        )
                    }
                    return@launch
                }

                // The initially selected service type should be the actual main service
                val preSelectedService = actualMainService

                // Update the state with all the data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mainService = actualMainService,
                        pets = petsList,
                        relatedServices = relatedServices,
                        selectedServiceType = preSelectedService,
                        error = null
                    )
                }

                // Step 6: Fetch branches for the selected service
                fetchAvailableBranches(preSelectedService.serviceId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Fetches branches that offer a specific service and updates the UI state.
     * This function is called both initially and when a new service type is selected.
     */
    private fun fetchAvailableBranches(newServiceId: String) {
        viewModelScope.launch {
            try {
                val branchesResult = branchRepository.getAvailableBranchesForService(newServiceId)
                _uiState.update {
                    it.copy(
                        availableBranches = branchesResult.getOrElse { emptyList() },
                        selectedBranch = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        availableBranches = emptyList(),
                        selectedBranch = null
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

    /**
     * Handles the user selecting a new service type from the list.
     * It updates the selected service and fetches the branches that offer it.
     */
    fun onServiceTypeSelected(service: Service) {
        _uiState.update {
            it.copy(
                selectedServiceType = service,
                selectedBranch = null,
                availableBranches = emptyList()
            )
        }
        fetchAvailableBranches(service.serviceId)
    }
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}