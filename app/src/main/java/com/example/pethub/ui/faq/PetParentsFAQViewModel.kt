package com.example.pethub.ui.faq

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


data class QAItem(val question: String, val answer: String)


data class PetParentsFaqUiState(
    val questions: List<QAItem> = emptyList()
)

@HiltViewModel
class PetParentsFAQViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PetParentsFaqUiState())
    val uiState: StateFlow<PetParentsFaqUiState> = _uiState.asStateFlow()

    init {
        loadPetParentsQuestions()
    }

    private fun loadPetParentsQuestions() {
        val qaList = listOf(
            QAItem(
                question = "How does PetHub work?",
                answer = "\uD83D\uDC3E **Booking Pet Services**\n" +
                        "PetHub allows pet parents to easily book services such as training, grooming, daycare, boarding, and pet walking. Simply log in, choose the service you need, select your pet, pick a date and time, and confirm your booking through the app.\n" +
                        "\n"+
                        "\uD83D\uDECD\uFE0F **Pet Shop & Menu Browsing**\n" +
                        "PetHub also provides a shop feature where users can browse pet products through a menu and purchase items directly in the app. This allows pet parents to get pet supplies and services in one place.\n" +
                        "\n"+
                        "\uD83C\uDFEC Payment at Shop\n"+
                        "Payments are made directly at the shop or service location, not through the app.\n"+
                        "\n"+
                        "\uD83D\uDD12 **Trusted Platform**\n" +
                        "PetHub is a community built for pet lovers. To protect users, all bookings should be handled within the PetHub system. This ensures transparency, safety, and better service quality for everyone.\n"
            ),
            QAItem(
                question = "Can I view my pet's profile?",
                answer = "Yes, you can view and edit your pet's profile from the **Profile** tab. You can update their photo, name, age, weight, and other important details."
            ),
            QAItem(
                question = "Do I need to create an account?",
                answer = "Yes, an account is required to use all PetHub features."
            ),
            QAItem(
                question = "Can I use one account for multiple pets?",
                answer = "Yes, you can add and manage multiple pets in one account."
            ),
            QAItem(
                question = "How do I update my personal information?",
                answer = "Go to **Profile > Edit Profile** to update your details."
            ),
            QAItem(
                question = "Is PetHub a Trusted Platform?",
                answer = "**We’d like to think so!**\n" +
                        "✨ **Built on Trust**\n" +
                        "PetHub connects pet parents with trusted service providers for training, grooming, daycare, boarding, and pet walking.\n" +
                        "\n" +
                        "\uD83C\uDF93 **Qualified Service Providers**\n" +
                        "All service providers listed on PetHub have relevant professional training, certification, or experience related to pet services such as grooming, training, daycare, boarding, and walking."
            )
        )
        _uiState.value = PetParentsFaqUiState(questions = qaList)
    }
}
