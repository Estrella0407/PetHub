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
    // This screen does not need the full user pet list, only the selected one.
    // val userPets: List<Pet> = emptyList(),
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
        // FIX: Get ALL required IDs from navigation arguments passed from the previous screen.
        val serviceId: String? = savedStateHandle.get("serviceId")
        val petId: String? = savedStateHandle.get("petId")
        val branchId: String? = savedStateHandle.get("branchId")

        if (serviceId == null || petId == null || branchId == null) {
            _uiState.value = BookAppointmentUiState(
                isLoading = false,
                error = "Required booking information is missing."
            )
        } else {
            // Pass all three IDs to the loading function
            loadInitialData(serviceId, petId, branchId)
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

    // This function now correctly loads all data needed JUST for this screen.
    private fun loadInitialData(serviceId: String, petId: String, branchId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value =
                        _uiState.value.copy(isLoading = false, error = "User not logged in.")
                    return@launch
                }

                // Fetch all data fresh from repositories using the IDs. This is robust.
                val serviceResult = serviceRepository.getServiceById(serviceId)
                val petResult = petRepository.getPetById(userId, petId)
                val branchResult = branchRepository.getBranchById(branchId)
                val appointmentsResult = appointmentRepository.getAllAppointments()

                val service = serviceResult.getOrNull()?.toServiceItem()
                val pet = petResult.getOrNull()
                val branch = branchResult.getOrNull()
                val existingAppointments = appointmentsResult.getOrNull() ?: emptyList()

                if (service == null || pet == null || branch == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load booking details."
                    )
                    return@launch
                }

                // Update the state with the freshly loaded, guaranteed up-to-date data.
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    service = service,
                    selectedPet = pet,
                    selectedBranch = branch,
                    existingAppointments = existingAppointments
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    private fun calculateTimeSlots(
        date: LocalDate,
        branch: Branch?,
        appointments: List<com.example.pethub.data.model.Appointment>
    ): List<TimeSlot> {
        if (branch == null) return emptyList()

        // 1. Determine the day of the week from the selected date.
        val dayOfWeek = date.dayOfWeek // e.g., MONDAY, SATURDAY

        // 2. Define the start and end hours based on the day of the week.
        val startHour = 10
        val endHour = if (dayOfWeek in java.time.DayOfWeek.SATURDAY..java.time.DayOfWeek.SUNDAY) {
            18
        } else {
            // It's a weekday, so operating hours end at 8 PM (last slot is 7 PM).
            20 // Represents the 8pm
        }

        // 3. Generate the list of all possible slots for that day within operating hours.
        val allSlots = (startHour..endHour).map { hour -> LocalTime.of(hour, 0) }

        // --- The rest of your logic remains the same ---

        // 4. Find which slots are already booked for that specific day and branch.
        val bookedSlotsForDay = appointments.filter {
            val apptDate =
                it.dateTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            it.branchId == branch.branchId && apptDate == date
        }.map {
            it.dateTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
        }.toSet()

        // 5. Create the final list of TimeSlot objects, marking booked ones as unavailable.
        return allSlots.map { slot ->
            TimeSlot(time = slot, isAvailable = !bookedSlotsForDay.contains(slot))
        }
    }


    private fun updateAvailableTimeSlots(
        date: LocalDate,
        branch: Branch?,
        appointments: List<com.example.pethub.data.model.Appointment>
    ) {
        _uiState.value = _uiState.value.copy(
            availableTimeSlots = calculateTimeSlots(date, branch, appointments)
        )
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date, selectedTimeSlot = null)
    }

    fun onMonthChanged(forward: Boolean) {
        val newMonth =
            if (forward) _uiState.value.currentDisplayMonth.plusMonths(1) else _uiState.value.currentDisplayMonth.minusMonths(
                1
            )
        _uiState.value = _uiState.value.copy(currentDisplayMonth = newMonth)
    }

    fun onTimeSlotSelected(time: LocalTime) {
        _uiState.value = _uiState.value.copy(selectedTimeSlot = time)
    }

    fun onSpecialInstructionsChanged(text: String) {
        _uiState.value = _uiState.value.copy(specialInstructions = text)
    }

    fun confirmBooking() {
        val state = _uiState.value

        // Get all necessary IDs safely.
        val serviceId = state.service?.id
        val petId = state.selectedPet?.petId
        val branchId = state.selectedBranch?.branchId
        val timeSlot = state.selectedTimeSlot

        // Validate that all required information is present.
        if (serviceId == null || petId == null || branchId == null || timeSlot == null) {
            _uiState.value =
                state.copy(error = "Please ensure a pet, service, branch, and time slot are all selected.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(bookingInProgress = true)
            val zonedDateTime = state.selectedDate.atTime(timeSlot).atZone(ZoneId.systemDefault())
            val timestamp = Timestamp(Date.from(zonedDateTime.toInstant()))

            val newAppointment = com.example.pethub.data.model.Appointment(
                petId = petId,
                serviceId = serviceId,
                branchId = branchId,
                dateTime = timestamp,
                status = "Completed"
            )

            try {
                // Assuming your repository's createAppointment function adds the document
                // and lets Firestore generate the ID.
                appointmentRepository.createAppointment(newAppointment)
                _uiState.value =
                    _uiState.value.copy(bookingInProgress = false, bookingSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    bookingInProgress = false,
                    error = e.message ?: "Booking failed."
                )
            }
        }
    }
}