package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SupportHelpFaqUiState(
    val questions: List<QAItem> = emptyList()
)

@HiltViewModel
class SupportHelpFAQViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SupportHelpFaqUiState())
    val uiState: StateFlow<SupportHelpFaqUiState> = _uiState.asStateFlow()

    init {
        loadSupportHelpQuestions()
    }

    private fun loadSupportHelpQuestions() {
        val qaList = listOf(
            QAItem(
                question = "How do I contact PetHub support?",
                answer = "You can contact our support team via email at support@pethubkl.com or support@pethubpj.com. You may also call the phone number listed on our homepage depending on the branch you are visiting."
            ),
            QAItem(
                question = "What should I do if the app crashes or does not work properly?",
                answer = "First, try restarting the app. If the problem persists, you can report the issue to our support team via email or phone for assistance."
            ),
            QAItem(
                question = "Can I give feedback about a service?",
                answer = "Feedback about services can only be provided directly in the shop to the staff after your service is completed. PetHub does not collect service ratings or reviews in the app."
            ),
            QAItem(
                question = "Where can I report a problem with a booking or service?",
                answer = "Please inform the staff at the shop directly. They will assist you with any issues related to your booking or service."
            ),
            QAItem(
                question = "Does PetHub handle complaints online?",
                answer = "No. All complaints or service issues must be handled directly with the shop staff. PetHub only provides the booking system and contact information."
            )
        )
        _uiState.value = SupportHelpFaqUiState(questions = qaList)
    }
}
