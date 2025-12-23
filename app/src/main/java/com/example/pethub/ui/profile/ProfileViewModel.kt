package com.example.pethub.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.example.pethub.data.model.Appointment
import com.example.pethub.data.model.AppointmentItem
import com.example.pethub.data.model.Customer
import com.example.pethub.data.model.Order
import com.example.pethub.data.model.OrderItem
import com.example.pethub.data.model.Pet
import com.example.pethub.data.repository.AppointmentRepository
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.CustomerRepository
import com.example.pethub.data.repository.OrderRepository
import com.example.pethub.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val appointmentRepository: AppointmentRepository,
    private val orderRepository: OrderRepository,
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var dataLoadingJob: Job? = null

    init {
        loadCustomerData()
        checkAndCompletePastOrders()
    }

    private fun checkAndCompletePastOrders() {
        viewModelScope.launch {
            try {
                val pendingOrders = orderRepository.getPendingOrdersForCurrentUser()
                val now = Timestamp.now()
                pendingOrders.forEach { order ->
                    val pickupTime = order.pickupDateTime
                    if (pickupTime != null && pickupTime < now) {
                        orderRepository.updateOrderStatus(order.orderId, "Completed")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadCustomerData() {
        // Cancel previous job if any
        dataLoadingJob?.cancel()

        dataLoadingJob = viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // Combine the flows of raw data
                combine(
                    customerRepository.listenToCurrentCustomer(),
                    appointmentRepository.getUpcomingAppointments(limit = 2),
                    orderRepository.getOrdersForCurrentUser(limit = 2),
                    petRepository.getPetsForCurrentUser()
                ) { customer, appointments, orders, pets ->
                    // Return a data class holding the RAW data
                    ProfileRawData(customer, appointments, orders, pets)
                }
                    .collect { rawData ->
                        // Inside 'collect' (which IS a suspend context), perform the async conversions

                        // Convert appointments to AppointmentItems
                        // Use mapNotNull to filter out any failed conversions
                        val appointmentItems = rawData.appointments.mapNotNull { appointment ->
                            try {
                                // getAppointmentItem returns Result<AppointmentItem?>
                                appointmentRepository.getAppointmentItem(appointment).getOrNull()
                            } catch (e: Exception) {
                                println("DEBUG: Failed to convert appointment ${appointment.appointmentId}: ${e.message}")
                                null
                            }
                        }

                        // Convert orders to OrderItems
                        val orderItems = rawData.orders.mapNotNull { order ->
                            try {
                                orderRepository.getOrderItem(order)
                            } catch (e: Exception) {
                                println("DEBUG: Failed to convert order ${order.orderId}: ${e.message}")
                                null
                            }
                        }

                        _uiState.value = ProfileUiState.Success(
                            customer = rawData.customer,
                            appointments = appointmentItems,
                            orders = orderItems,
                            pets = rawData.pets
                        )
                    }
            } catch (e: Exception) {
                println("DEBUG: ProfileViewModel error: ${e.message}")
                e.printStackTrace()
                _uiState.value = ProfileUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            // check current state just for the initial check
            if (_uiState.value is ProfileUiState.Success) {
                try {
                    // Start Uploading
                    _uiState.update { state ->
                        if (state is ProfileUiState.Success) {
                            state.copy(isUploading = true, uploadProgress = 0f)
                        } else state
                    }

                    customerRepository.uploadProfileImage(uri) { progress ->
                        // Update Progress safely
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                state.copy(uploadProgress = progress)
                            } else state
                        }
                    }.onSuccess { imageUrl ->
                        // Update Firestore
                        customerRepository.updateProfileImageUrl(imageUrl)

                        // Stop uploading.
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                state.copy(isUploading = false)
                            } else state
                        }
                    }.onFailure { e ->
                        // Handle failure safely
                        _uiState.update { state ->
                            if (state is ProfileUiState.Success) {
                                ProfileUiState.Error("Upload failed: ${e.message}", customer = state.customer)
                            } else state
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception safely
                    _uiState.update { state ->
                        if (state is ProfileUiState.Success) {
                            ProfileUiState.Error("Upload failed", customer = state.customer)
                        } else state
                    }
                }
            }
        }
    }

    fun logout() {
        dataLoadingJob?.cancel()

        // This detaches the sign-out action from the lifecycle of the data loading job.
        viewModelScope.launch {
            // Optional but recommended: Wait for the cancellation to fully complete.
            // This gives the listeners a chance to detach before auth state changes.
            dataLoadingJob?.join()

            authRepository.signOut()
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataLoadingJob?.cancel()
    }
}

sealed class ProfileUiState(open val customer: Customer? = null) {
    data object Loading : ProfileUiState()

    data class Success(
        override val customer: Customer?,
        val appointments: List<AppointmentItem> = emptyList(),
        val orders: List<OrderItem> = emptyList(),
        val pets: List<Pet> = emptyList(),
        val isUploading: Boolean = false,
        val uploadProgress: Float = 0f
    ) : ProfileUiState(customer)

    data class Error(
        val message: String,
        override val customer: Customer? = null
    ) : ProfileUiState(customer)
}

data class ProfileRawData(
    val customer: Customer?,
    val appointments: List<Appointment>,
    val orders: List<Order>,
    val pets: List<Pet>
)