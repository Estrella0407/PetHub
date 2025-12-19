package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class PolicyFaqUiState {
    data object Loading : PolicyFaqUiState()
    data class Success(val questions: List<QAItem>) : PolicyFaqUiState()
    data class Error(val message: String) : PolicyFaqUiState()
}

@HiltViewModel
class PolicyFAQViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<PolicyFaqUiState>(PolicyFaqUiState.Loading)
    val uiState: StateFlow<PolicyFaqUiState> = _uiState.asStateFlow()

    init {
        loadPolicyQuestions()
    }

    private fun loadPolicyQuestions() {
        val qaList = listOf(
            QAItem(
                question = "What is PetHub’s booking policy?",
                answer = "All bookings must be made through the app. Make sure to provide accurate pet and contact details."
            ),
            QAItem(
                question = "What is PetHub’s payment policy?",
                answer = "Payment is made at the shop during or after the service. PetHub does not process payments in the app. Prices shown include tax and will be calculated before confirming your booking."
            ),
            QAItem(
                question = "How does PetHub protect user data?",
                answer = "All data is stored securely and privately."
            ),
            QAItem(
                question = "Does PetHub share my data with others?",
                answer = "No, data is only shared with authorized providers."
            ),
            QAItem(
                question = "What happens in case of an emergency?",
                answer = "Service providers will contact pet parents immediately."
            )
        )
        _uiState.value = PolicyFaqUiState.Success(qaList)
    }

    fun retry() {
        loadPolicyQuestions()
    }
}
