package com.example.pethub.ui.shop

import androidx.lifecycle.ViewModel
import com.example.pethub.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<ShopUiState>(ShopUiState.Success)
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Pet Food")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val allProducts = listOf(
            Product(
                id = "1",
                name = "Royal Canin Sterilised 37",
                description = "10kg Dry Cat Food",
                price = 353.10,
                category = "Pet Food",
                imageUrl = "https://example.com/royal_canin_sterilised.jpg" // Placeholder
            ),
            Product(
                id = "2",
                name = "Royal Canin Maxi Adult",
                description = "15kg Dry Dog Food",
                price = 370.50,
                category = "Pet Food",
                imageUrl = "https://example.com/royal_canin_maxi.jpg"
            ),
            Product(
                id = "3",
                name = "Monge Fresh Salmon",
                description = "100g Wet Dog Food",
                price = 4.20,
                category = "Pet Food",
                imageUrl = "https://example.com/monge_salmon.jpg"
            ),
            Product(
                id = "4",
                name = "Aatas Cat",
                description = "80g Tantalizing Tuna & Saba In Aspic Wet Cat Canned Food",
                price = 3.30,
                category = "Pet Food",
                imageUrl = "https://example.com/aatas_cat.jpg"
            ),
             Product(
                id = "5",
                name = "Dentastix",
                description = "Daily Oral Care",
                price = 15.50,
                category = "Pet Treats",
                imageUrl = ""
            ),
            Product(
                id = "6",
                name = "Squeaky Mouse",
                description = "Plush Toy",
                price = 8.90,
                category = "Pet Toys",
                imageUrl = ""
            ),
             Product(
                id = "7",
                name = "Pet Shampoo",
                description = "Sensitive Skin 500ml",
                price = 25.00,
                category = "Grooming Products",
                imageUrl = ""
            )
        )
        _products.value = allProducts
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(product: Product) {
        _cartItemCount.value += 1
    }
}

sealed class ShopUiState {
    object Loading : ShopUiState()
    object Success : ShopUiState()
    data class Error(val message: String) : ShopUiState()
}
