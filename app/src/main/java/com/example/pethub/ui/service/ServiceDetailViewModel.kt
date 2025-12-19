package com.example.pethub.ui.service

import android.util.Log
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
private const val TAG = "ServiceDetailDebug"
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
        Log.d(TAG, "ViewModel initialized for serviceId: $serviceId")
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "--- Starting loadInitialData ---")
            try {
                // Fetch the main service to get its category, image, etc.
                val mainService = serviceRepository.getServiceById(serviceId).getOrNull()
                Log.d(TAG, "1. Fetched mainService: ${mainService?.serviceName ?: "NULL"}")

                // Fetch pets and related services
                val petsList = petRepository.getPetsForCurrentUser().first() // Use first() to get the current list from the Flow
                Log.d(TAG, "2. Fetched ${petsList.size} pets.")

                val categoryToFetch = mainService?.serviceName ?: ""
                Log.d(TAG, "3. Fetching related services for category: '$categoryToFetch'")
                val relatedServicesResult = serviceRepository.getServicesByCategory(categoryToFetch)

                _uiState.update {
                    val relatedServices = relatedServicesResult.getOrElse {
                        Log.e(TAG, "Error fetching related services", it)
                        emptyList()
                    }
                    Log.d(TAG, "4. Updating state with ${relatedServices.size} related services.")
                    val initialService = relatedServices.find { s -> s.serviceId == serviceId }
                    Log.d(TAG, "5. Pre-selecting service type: ${initialService?.serviceName ?: "NULL"}")

                    it.copy(
                        isLoading = false,
                        mainService = mainService,
                        pets = petsList,
                        relatedServices = relatedServices,
                        // Pre-select the service type based on the initial serviceId
                        selectedServiceType = initialService
                    )
                }
                Log.d(TAG, "6. UI state updated.")

                // After setting the initial state, fetch the branches for the pre-selected service
                val selectedService = _uiState.value.selectedServiceType
                if (selectedService != null) {
                    Log.d(TAG, "7. Fetching branches for pre-selected service: '${selectedService.serviceName}' (ID: ${selectedService.serviceId})")
                    fetchAvailableBranches(selectedService.serviceId)
                } else {
                    Log.w(TAG, "7. No pre-selected service, skipping initial branch fetch.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "--- CRITICAL FAILURE in loadInitialData ---", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load data: ${e.message}") }
            }
        }
    }

    // This function is called every time a new service type is selected
    private fun fetchAvailableBranches(serviceId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Fetching available branches for serviceId: $serviceId")
            val branchesResult = branchRepository.getBranchesOfferingService(serviceId)
            _uiState.update {
                val branches = branchesResult.getOrElse {
                    Log.e(TAG, "Failed to get branches for serviceId: $serviceId", it)
                    emptyList()
                }
                Log.d(TAG, "Found ${branches.size} branches. Updating state.")
                it.copy(
                    availableBranches = branches,
                    // Reset branch selection when the service type changes
                    selectedBranch = null
                )
            }
        }
    }

    fun onPetSelected(pet: Pet) {
        Log.d(TAG, "Pet selected: ${pet.petName}")
        _uiState.update { it.copy(selectedPet = pet) }
    }

    fun onServiceTypeSelected(service: Service) {
        Log.d(TAG, "Service type selected: ${service.serviceName}")
        _uiState.update { it.copy(selectedServiceType = service) }
        // When a new service type is selected, re-fetch the branches that offer it.
        fetchAvailableBranches(service.serviceId)
    }

    fun onBranchSelected(branch: Branch) {
        Log.d(TAG, "Branch selected: ${branch.branchName}")
        _uiState.update { it.copy(selectedBranch = branch) }
    }
}
