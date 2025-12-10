package com.example.pethub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.ServiceItem
import com.example.pethub.data.model.toServiceItem
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.BranchRepository
import com.example.pethub.data.repository.CustomerRepository
import com.example.pethub.data.repository.PetRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Home Screen
 * Manages home screen data and business logic
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
    private val petRepository: PetRepository,
    private val serviceRepository: ServiceRepository,
    private val branchRepository: BranchRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // User name for greeting
    private val _userName = MutableStateFlow("Guest")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Recommended/All services
    private val _recommendedServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val recommendedServices: StateFlow<List<ServiceItem>> = _recommendedServices.asStateFlow()

    // Expose the full list of pets to the UI
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    // Keep track of the currently selected pet
    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    // Function to be called from the UI when a new pet is chosen
    fun onPetSelected(pet: Pet) {
        _selectedPet.value = pet

        // Launch a coroutine to call the suspend function
        viewModelScope.launch {
            // Re-fetch recommended services that depend on the pet
            val result = serviceRepository.loadRecommendedServices(pet.petId)

            // Update the UI state based on the result
            result.onSuccess { services ->
                _recommendedServices.value = services.map { it.toServiceItem() }
            }.onFailure {
                // Optional: If recommendation fails,
                // keep showing the previous list or log the error
            }
        }
    }


    // Available branches
    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    init {
        // When the full pet list loads, select the first one as the default
        viewModelScope.launch {
            pets.collect { petList ->
                if (_selectedPet.value == null && petList.isNotEmpty()) {
                    _selectedPet.value = petList.first()
                }
            }
        }
        loadData()
    }

    /**
     * Load all home screen data
     */
    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                // Get current user ID
                val userId = authRepository.getCurrentUserId()
                
                if (userId != null) {
                    // Load user data
                    loadCustomerData()

                    // Load pets (launch in new coroutine to avoid blocking)
                    launch { loadUserPets(userId) }
                } else {
                    _userName.value = "Guest"
                }

                // Load services (launch in new coroutine to avoid blocking)
                launch { loadServices() }
                
                // Load branches
                launch { loadBranches() }

                _uiState.value = HomeUiState.Success

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "An error occurred while loading data"
                )
            }
        }
    }

    /**
     * Load customer profile data
     */
    private suspend fun loadCustomerData() {
        try {
            val result = customerRepository.getCurrentCustomer()
            val customer = result.getOrNull()
            _userName.value = customer?.custName?.takeIf { it.isNotEmpty() } 
                ?: customer?.custEmail?.takeIf { it.isNotEmpty() } 
                ?: "Guest"
        } catch (e: Exception) {
            _userName.value = "Guest"
        }
    }

    /**
     * Load user's pets
     */
    private suspend fun loadUserPets(userId: String) {
        // Ensure listening to the flow from the repository
        petRepository.listenToUserPets(userId).collect { petsList ->
            _pets.value = petsList // <--- Update _pets here

            // Set first pet as selected by default if none selected
            if (petsList.isNotEmpty() && _selectedPet.value == null) {
                _selectedPet.value = petsList.first()
            }
        }
    }


    /**
     * Load all active services
     */
    private suspend fun loadServices() {
        serviceRepository.listenToServices().collect { services ->
            _recommendedServices.value = services.map { it.toServiceItem() }
        }
    }
    
    /**
     * Load all branches
     */
    private suspend fun loadBranches() {
        branchRepository.listenToBranches().collect { branchesList ->
            _branches.value = branchesList
        }
    }

    /**
     * Select a different pet
     */
    fun selectPet(pet: Pet) {
        _selectedPet.value = pet
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadData()
    }

    /**
     * Check if user is admin
     */
    suspend fun isAdmin(): Boolean {
        return authRepository.isAdmin()
    }
}

/**
 * UI State sealed class
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
