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
import com.example.pethub.data.repository.NotificationRepository
import com.example.pethub.data.repository.PetRepository
import com.example.pethub.data.repository.ServiceRepository
import com.example.pethub.utils.isServiceSuitableForPet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
    private val petRepository: PetRepository,
    private val serviceRepository: ServiceRepository,
    private val branchRepository: BranchRepository,
    private val notificationRepository: NotificationRepository
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

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    private var recommendationJob: Job? = null
    private var notificationListenerJob: Job? = null

    init {
        loadData()
        // Start listening for notifications immediately
        startNotificationListener()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                loadCustomerData()
                // Fetch initial pets and recommendations before setting the Success state
                val initialPets = petRepository.getCurrentUserPets().getOrNull() ?: emptyList()
                _pets.value = initialPets
                if (initialPets.isNotEmpty()) {
                    val firstPet = initialPets.first()
                    _selectedPet.value = firstPet
                    loadRecommendedServicesForPet(firstPet)
                } else {
                    loadGenericServices()
                }
                // Start the listener for subsequent updates
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

    private fun startNotificationListener() {
        // Cancel any existing listener
        notificationListenerJob?.cancel()

        notificationListenerJob = viewModelScope.launch {
            try {
                notificationRepository.hasUnreadNotifications().collect { hasUnread ->
                    _hasUnreadNotifications.value = hasUnread
                }
            } catch (e: Exception) {
                // If there's an error, default to false
                _hasUnreadNotifications.value = false
            }
        }
    }

    // This listener is now only for updates after the initial load
    private fun listenForUserPets(userId: String) {
        viewModelScope.launch {
            petRepository.listenToUserPets(userId).collect { petsList ->
                _pets.value = petsList
                // If the selected pet was deleted, select the new first pet
                if (petsList.isNotEmpty() && !_pets.value.contains(_selectedPet.value)) {
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
            val allServices = serviceRepository.listenToServices().first()
            _recommendedServices.value = allServices.map { it.toServiceItem() }
        }
    }

    private fun loadRecommendedServicesForPet(pet: Pet) {
        recommendationJob?.cancel()
        recommendationJob = viewModelScope.launch {
            try {
                // Fetch all services once.
                val allServices = serviceRepository.listenToServices().first()
                // Filter the list using your utility function.
                val suitableServices = allServices.filter { service ->
                    isServiceSuitableForPet(service.type, pet.type)
                }
                _recommendedServices.value = suitableServices.map { it.toServiceItem() }
            } catch (e: Exception) {
                // If there's an error, fallback to showing everything.
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
        if (_selectedPet.value?.petId != pet.petId) {
            _selectedPet.value = pet
            loadRecommendedServicesForPet(pet)
        }
    }

    fun refresh() {
        loadData()
        // Restart the notification listener as well
        startNotificationListener()
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up jobs when ViewModel is destroyed
        notificationListenerJob?.cancel()
        recommendationJob?.cancel()
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}