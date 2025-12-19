package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// QAItem is now defined in its own file and is imported automatically.


sealed class PaymentFaqUiState {
    data object Loading : PaymentFaqUiState()
    data class Success(val questions: List<QAItem>) : PaymentFaqUiState()
    data class Error(val message: String) : PaymentFaqUiState()
}

@HiltViewModel
class PaymentFAQViewModel @Inject constructor() : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow<PaymentFaqUiState>(PaymentFaqUiState.Loading)
    val uiState: StateFlow<PaymentFaqUiState> = _uiState.asStateFlow()

    init {
        loadPaymentQuestions()
    }

    private fun loadPaymentQuestions() {
        val qaList = listOf(
            QAItem(
                question = "What payment methods does PetHub support?",
                answer = "PetHub supports cash, online banking, and e-wallet payments.\n" +
                        "All payments are **made directly at the shop or service location**, not within the app."
            ),
            QAItem(
                question = "Can I pay later?",
                answer = "No. Payment must be made during or immediately after the service at the shop."
            ),
            QAItem(
                question = "Will I receive a payment receipt?",
                answer = "Yes. You may request a receipt from the staff at the shop after making payment."
            ),
            QAItem(
                question = "How do I get a refund?",
                answer = "Refunds are not processed through the PetHub system.\n" +
                        "Any refund request must be discussed directly with the shop or service provider.\n" +
                        "Refund approval depends on the shop’s policy and the situation, such as service cancellation or unexpected issues."
            ),
            QAItem(
                question = "What can I do if I am not satisfied with the service?",
                answer = "If you are not satisfied, please inform the staff or service provider at the shop as soon as possible.\n" +
                        "They will review the issue and may offer a solution based on their service policy.\n" +
                        "PetHub does not handle complaints, ratings, or refunds within the app."
            ),
            QAItem(
                question = "Are prices shown in the app final?",
                answer = "Yes. The final price, including tax, will be calculated and displayed in the app before you confirm your booking."
            )
        )
        // Set the state to Success with the loaded data
        _uiState.value = PaymentFaqUiState.Success(qaList)
    }

    fun retry() {
        loadPaymentQuestions()
    }
}
