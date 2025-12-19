package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Represents a single FAQ topic item.
 * @param id A unique identifier for navigation.
 * @param title The display name of the topic.
 */
data class FaqTopic(val id: String, val title: String)

/**
 * Defines the possible states for the FAQ Screen UI.
 */
sealed class FaqUiState {
    data object Loading : FaqUiState()
    data class Success(val topics: List<FaqTopic>) : FaqUiState()
    data class Error(val message: String) : FaqUiState()
}

/**
 * ViewModel for the FAQ Screen.
 * Manages the data (list of topics) to be displayed.
 */
@HiltViewModel
class FAQViewModel @Inject constructor() : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<FaqUiState>(FaqUiState.Loading)
    val uiState: StateFlow<FaqUiState> = _uiState.asStateFlow()

    init {
        loadFaqTopics()
    }

    /**
     * Loads the list of FAQ topics.
     * For now, this uses a hardcoded list. In the future, this could fetch from Firestore.
     */
    private fun loadFaqTopics() {
        val topics = listOf(
            FaqTopic("pet_parents", "\uD83D\uDC64 Pet Parents FAQ"),
            FaqTopic("pet_information", "\uD83D\uDC36 Pet Information FAQ"),
            FaqTopic("booking_appointment", "\uD83D\uDCC5 Booking & Appointments FAQ"),
            FaqTopic("cancellation_reschedule", "❌ Cancellation & Rescheduling FAQ"),
            FaqTopic("payments", "\uD83D\uDCB3 Payments FAQ"),
            FaqTopic("policies", "\uD83D\uDCDC Policies & Safety FAQ"),
            FaqTopic("support_help", "\uD83C\uDD98 Support & Help FAQ")
        )
        _uiState.value = FaqUiState.Success(topics)
    }
}
