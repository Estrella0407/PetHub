package com.example.pethub.ui.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.Pet
import com.example.pethub.data.model.ServiceItem
import com.example.pethub.data.model.toServiceItem
import com.example.pethub.data.repository.*
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

data class TimeSlot(
    val time: LocalTime,
    val isAvailable: Boolean,
    val bookedByPetName: String? = null
)

data class BookAppointmentUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val service: ServiceItem? = null,
    val userPets: List<Pet> = emptyList(),
    val availableBranches: List<Branch> = emptyList(),
    val existingAppointments: List<com.example.pethub.data.model.Appointment> = emptyList(),
    val selectedPet: Pet? = null,
    val selectedBranch: Branch? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTimeSlot: LocalTime? = null,
    val specialInstructions: String = "",
    val availableTimeSlots: List<TimeSlot> = emptyList(),
    val currentDisplayMonth: YearMonth = YearMonth.now(),
    val bookingInProgress: Boolean = false,
    val bookingSuccess: Boolean = false
)

@HiltViewModel
class BookAppointmentViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val petRepository: PetRepository,
    private val branchRepository: BranchRepository,
    private val appointmentRepository: AppointmentRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookAppointmentUiState())
    val uiState: StateFlow<BookAppointmentUiState> = _uiState.asStateFlow()

    init {
        val serviceId: String? = savedStateHandle.get<String>("serviceId")

        if (serviceId == null) {
            _uiState.value = BookAppointmentUiState(isLoading = false, error = "Service ID is missing.")
        } else {
            loadInitialData(serviceId)
        }

        // This reactive flow automatically updates the time slots whenever the selected date or branch changes.
        viewModelScope.launch {
            combine(
                _uiState.map { it.selectedDate },
                _uiState.map { it.selectedBranch },
                _uiState.map { it.existingAppointments }
            ) { date, branch, appointments ->
                Triple(date, branch, appointments)
            }.collect { (date, branch, appointments) ->
                updateAvailableTimeSlots(date, branch, appointments)
            }
        }
    }

    private fun loadInitialData(serviceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in.")
                    return@launch
                }

                val allServices = serviceRepository.listenToServices().first()
                val service = allServices.find { it.serviceId == serviceId }?.toServiceItem()

                if (service == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Service with ID '$serviceId' not found.")
                    return@launch
                }

                val petsResult = petRepository.getCustomerPets(userId)
                val branchesResult = branchRepository.getAllBranches()
                val appointmentsResult = appointmentRepository.getAllAppointments()

                val userPets = petsResult.getOrNull() ?: emptyList()
                val branches = branchesResult.getOrNull() ?: emptyList()
                val existingAppointments = appointmentsResult.getOrNull() ?: emptyList()

                val initialBranch = branches.firstOrNull()
                val initialTimeSlots = calculateTimeSlots(LocalDate.now(), initialBranch, existingAppointments)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    service = service,
                    userPets = userPets,
                    availableBranches = branches,
                    existingAppointments = existingAppointments,
                    selectedPet = userPets.firstOrNull(),
                    selectedBranch = initialBranch,
                    availableTimeSlots = initialTimeSlots
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load data: ${e.message}")
            }
        }
    }

    private fun calculateTimeSlots(date: LocalDate, branch: Branch?, appointments: List<com.example.pethub.data.model.Appointment>): List<TimeSlot> {
        if (branch == null) return emptyList()

        val allSlots = (9..16).map { LocalTime.of(it, 0) }
        val bookedSlotsForDay = appointments.filter {
            val apptDate = it.dateTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            it.branchId == branch.branchId && apptDate == date
        }.map {
            it.dateTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime() to it.petId
        }.toSet()

        return allSlots.map { slot ->
            val bookingInfo = bookedSlotsForDay.find { it.first == slot }
            if (bookingInfo != null) {
                TimeSlot(time = slot, isAvailable = false, bookedByPetName = "Booked")
            } else {
                TimeSlot(time = slot, isAvailable = true)
            }
        }
    }

    private fun updateAvailableTimeSlots(date: LocalDate, branch: Branch?, appointments: List<com.example.pethub.data.model.Appointment>) {
        _uiState.value = _uiState.value.copy(
            availableTimeSlots = calculateTimeSlots(date, branch, appointments)
        )
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date, selectedTimeSlot = null)
    }

    fun onMonthChanged(forward: Boolean) {
        val newMonth = if (forward) _uiState.value.currentDisplayMonth.plusMonths(1) else _uiState.value.currentDisplayMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(currentDisplayMonth = newMonth)
    }

    fun onTimeSlotSelected(time: LocalTime) {
        _uiState.value = _uiState.value.copy(selectedTimeSlot = time)
    }

    fun onBranchSelected(branch: Branch) {
        _uiState.value = _uiState.value.copy(selectedBranch = branch, selectedTimeSlot = null)
    }

    fun onSpecialInstructionsChanged(text: String) {
        _uiState.value = _uiState.value.copy(specialInstructions = text)
    }

    fun onPetSelected(pet: Pet) {
        _uiState.value = _uiState.value.copy(selectedPet = pet)
    }

    fun confirmBooking() {
        val userId = authRepository.getCurrentUserId()
        val serviceId: String? = savedStateHandle.get("serviceId")
        val state = _uiState.value

        if (userId == null || serviceId == null || state.selectedPet == null || state.selectedBranch == null || state.selectedTimeSlot == null) {
            _uiState.value = state.copy(error = "Please make sure a pet, branch, and time slot are selected.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(bookingInProgress = true)
            val zonedDateTime = state.selectedDate.atTime(state.selectedTimeSlot).atZone(ZoneId.systemDefault())
            val timestamp = Timestamp(Date.from(zonedDateTime.toInstant()))

            val newAppointment = com.example.pethub.data.model.Appointment(
                custId = userId,
                petId = state.selectedPet.petId,
                serviceId = serviceId,
                branchId = state.selectedBranch.branchId,
                dateTime = timestamp,
                status = "Pending",
                specialInstructions = state.specialInstructions
            )

            try {
                appointmentRepository.createAppointment(newAppointment)


                _uiState.value = _uiState.value.copy(bookingInProgress = false, bookingSuccess = true)

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(bookingInProgress = false, error = e.message ?: "Booking failed.")
            }
        }
    }
}
