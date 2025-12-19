package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BookingAppointmentFaqUiState(
    val questions: List<QAItem> = emptyList()
)

@HiltViewModel
class BookingAppointmentViewModelFAQ @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BookingAppointmentFaqUiState())
    val uiState: StateFlow<BookingAppointmentFaqUiState> = _uiState.asStateFlow()

    init {
        loadBookingAppointmentQuestions()
    }

    private fun loadBookingAppointmentQuestions() {
        val qaList = listOf(
            QAItem(
                question = "How do I book an appointment?",
                answer = "Navigate to the **Services** tab, select a service, choose your preferred date and time, and confirm your booking."
            ),
            QAItem(
                question = "Can I see my past and upcoming appointments?",
                answer = "Yes, all your past and upcoming appointments are listed in the **Appointment** section of your profile."
            ),
            QAItem(
                question = "Can I book more than one service at a time?",
                answer = "Yes, multiple services can be booked separately."
            ),
            QAItem(
                question = "How do I know my booking is confirmed?",
                answer = "A confirmation message will be shown in the app. "
            )

        )
        _uiState.value = BookingAppointmentFaqUiState(questions = qaList)
    }
}
