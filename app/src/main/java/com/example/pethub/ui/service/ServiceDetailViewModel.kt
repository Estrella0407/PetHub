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

    private val serviceId: String = savedStateHandle.get<String>("serviceId") ?: ""
    private val TAG = "ServiceDetailViewModel"

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Get user's pets
                val petsList = petRepository.getPetsForCurrentUser().first()
                
                // 2. Determine category name from serviceId (e.g. "grooming" -> "Grooming")
                val categoryName = serviceId.replaceFirstChar { it.uppercase() }
                Log.d(TAG, "Loading category: $categoryName")

                // 3. Fetch all services matching this category from 'service' collection
                val relatedResult = serviceRepository.getServicesByCategory(categoryName)
                val related = relatedResult.getOrElse { emptyList() }
                Log.d(TAG, "Found ${related.size} related services for category: $categoryName")

                // If nothing found by capitalized, try lowercase
                val finalRelated = if (related.isEmpty()) {
                    serviceRepository.getServicesByCategory(serviceId).getOrElse { emptyList() }
                } else {
                    related
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    relatedServices = finalRelated,
                    pets = petsList,
                    // Auto-select the first one or set mainService
                    selectedServiceType = finalRelated.firstOrNull(),
                    mainService = Service(serviceId = serviceId, serviceName = categoryName)
                ) }

                // 4. Initial fetch for branches using the serviceId (lowercase)
                fetchAvailableBranches(serviceId)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial data", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun fetchAvailableBranches(categoryServiceId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Fetching branches for: $categoryServiceId")
            val branchesResult = branchRepository.getAvailableBranchesForService(categoryServiceId)
            val branches = branchesResult.getOrElse { emptyList() }
            Log.d(TAG, "Found ${branches.size} available branches")
            
            _uiState.update { it.copy(
                availableBranches = branches,
                selectedBranch = null
            ) }
        }
    }

    fun onPetSelected(pet: Pet) = _uiState.update { it.copy(selectedPet = pet) }

    fun onServiceTypeSelected(service: Service) {
        _uiState.update { it.copy(selectedServiceType = service) }
        // Location depends on the category, so we keep using serviceId
        fetchAvailableBranches(serviceId)
    }

    fun onBranchSelected(branch: Branch) = _uiState.update { it.copy(selectedBranch = branch) }
}
