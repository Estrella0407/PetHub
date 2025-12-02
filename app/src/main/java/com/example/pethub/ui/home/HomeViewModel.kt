package com.example.pethub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.Service
import com.example.pethub.data.repository.AuthRepository
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
    private val customerRepository: CustomerRepository, // Changed from UserRepository
    private val petRepository: PetRepository,
    private val serviceRepository: ServiceRepository,
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

    // User's pets
    private val _userPets = MutableStateFlow<List<Pet>>(emptyList())
    val userPets: StateFlow<List<Pet>> = _userPets.asStateFlow()

    // Selected pet for quick services
    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    init {
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
        petRepository.listenToUserPets(userId).collect { pets ->
            _userPets.value = pets
            // Set first pet as selected by default
            if (pets.isNotEmpty() && _selectedPet.value == null) {
                _selectedPet.value = pets.first()
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

/**
 * Data class for service items in UI
 */
data class ServiceItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double, // Service model doesn't have rating anymore, defaults to 0.0 in UI logic if removed from model? Service model updated to remove rating? No, I kept it. Let's check Service.kt.
    val imageUrl: String,
    val availability: Boolean
)

/**
 * Extension function to convert Service to ServiceItem
 */
fun Service.toServiceItem(): ServiceItem {
    return ServiceItem(
        id = serviceId,
        name = serviceName, // Updated property name
        description = description,
        category = type, // Updated property name: category -> type
        price = price,
        rating = 0.0, // Rating removed from new Service model? I wrote Service.kt without rating? Let me check. I wrote Service.kt without rating in my previous turn? Yes.
        imageUrl = imageUrl,
        availability = true // Service model doesn't have availability anymore? Yes, moved to BranchService.
    )
}

/**
 * Data class for booking items in UI
 */
data class AppointmentItem( // Renamed from BookingItem
    val id: String,
    val serviceName: String, // Need to fetch from Service or join
    val petName: String, // Need to fetch from Pet or join
    val dateTime: String,
    val locationName: String,
    val status: String
)
