package com.example.pethub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.ServiceItem
import com.example.pethub.data.model.toServiceItem
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.BranchRepository
import com.example.pethub.data.repository.CustomerRepository
import com.example.pethub.data.repository.PetRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
    private val petRepository: PetRepository,
    private val serviceRepository: ServiceRepository,
    private val branchRepository: BranchRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("Guest")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _recommendedServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val recommendedServices: StateFlow<List<ServiceItem>> = _recommendedServices.asStateFlow()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private var recommendationJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                loadCustomerData()
                listenForUserPets(userId)
            } else {
                _userName.value = "Guest"
                loadGenericServices()
            }
            launch { loadBranches() }
            _uiState.value = HomeUiState.Success
        }
    }

    private suspend fun loadCustomerData() {
        try {
            val result = customerRepository.getCurrentCustomer()
            val customer = result.getOrNull()
            _userName.value = customer?.custName?.takeIf { it.isNotEmpty() } ?: "Guest"
        } catch (e: Exception) {
            _userName.value = "Guest"
        }
    }

    private fun listenForUserPets(userId: String) {
        viewModelScope.launch {
            petRepository.listenToUserPets(userId).collect { petsList ->
                _pets.value = petsList
                if (petsList.isNotEmpty() && _selectedPet.value == null) {
                    selectPet(petsList.first())
                } else if (petsList.isEmpty()) {
                    _selectedPet.value = null
                    loadGenericServices()
                }
            }
        }
    }

    private fun loadGenericServices() {
        recommendationJob?.cancel()
        recommendationJob = viewModelScope.launch {
            serviceRepository.listenToServices().collect { services ->
                _recommendedServices.value = services.map { it.toServiceItem() }
            }
        }
    }

    private fun loadRecommendedServicesForPet(pet: Pet) {
        recommendationJob?.cancel()
        recommendationJob = viewModelScope.launch {
            try {
                // MODIFIED: Pass pet.breed instead of pet.weight
                val services = appointmentRepository.getRecommendedServicesForPet(
                    petType = pet.type,
                    petBreed = pet.breed
                )
                _recommendedServices.value = services.map { it.toServiceItem() }
            } catch (e: Exception) {
                loadGenericServices()
            }
        }
    }

    private suspend fun loadBranches() {
        branchRepository.listenToBranches().collect { branchesList ->
            _branches.value = branchesList
        }
    }

    fun selectPet(pet: Pet) {
        // Always update the selected pet and trigger new recommendations
        // if the pet ID is different from the currently selected one.
        if (_selectedPet.value?.petId != pet.petId) {
            _selectedPet.value = pet
            loadRecommendedServicesForPet(pet)
        }
    }

    fun refresh() {
        loadData()
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
