package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CancellationRescheduleFaqUiState(
    val questions: List<QAItem> = emptyList()
)

@HiltViewModel
class CancellationRescheduleFAQViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CancellationRescheduleFaqUiState())
    val uiState: StateFlow<CancellationRescheduleFaqUiState> = _uiState.asStateFlow()

    init {
        loadCancellationRescheduleQuestions()
    }

    private fun loadCancellationRescheduleQuestions() {
        val qaList = listOf(
            QAItem(
                question = "Can I cancel my booking?",
                answer = "Yes, bookings can be cancelled before the service date."
            ),
            QAItem(
                question = "How do I reschedule an appointment?",
                answer = "To reschedule, go to your booking and select reschedule."
            ),
            QAItem(
                question = "What happens if I miss my appointment?",
                answer = "If you do not attend your appointment, the service provider may call to confirm. If there is no reply, no refund will be given."
            )
        )
        _uiState.value = CancellationRescheduleFaqUiState(questions = qaList)
    }
}
