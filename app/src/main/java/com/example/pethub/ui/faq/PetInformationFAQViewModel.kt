package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PetInformationFaqUiState(
    val questions: List<QAItem> = emptyList()
)

@HiltViewModel
class PetInformationFAQViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PetInformationFaqUiState())
    val uiState: StateFlow<PetInformationFaqUiState> = _uiState.asStateFlow()

    init {
        loadPetInformationQuestions()
    }

    private fun loadPetInformationQuestions() {
        val qaList = listOf(
            QAItem(
                question = "How do I add a new pet?",
                answer = "You can add a new pet from your profile screen by tapping the **Add Pet** button."
            ),
            QAItem(
                question = "What information can I store for my pet?",
                answer = "You can store your pet's name, type, breed, date of birth, sex, weight, and special remarks."
            ),
            QAItem(
                question = "Can I edit or remove a pet profile?",
                answer = "Yes, pet profiles can be edited or removed anytime."
            ),
            QAItem(
                question = "Who can see my pet’s information?",
                answer = "Only you can view it, service providers can view it by scanning the QR generated for each pet."
            ),
        )
        _uiState.value = PetInformationFaqUiState(questions = qaList)
    }
}
