package com.example.pethub.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    // 1. Get Real Cart Items from Firebase
    val cartItems = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Calculate Total Amount Automatically
    val totalAmount = cartItems.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val branchList: StateFlow<List<String>> = repository.getBranches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // 初始为空
        )
    // 3. UI State for User Selections
    private val _selectedBranch = MutableStateFlow("Select a Branch")
    val selectedBranch = _selectedBranch.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow("")
    val selectedTime = _selectedTime.asStateFlow()

    // --- Actions ---

    fun updateBranch(branch: String) {
        _selectedBranch.value = branch
    }

    fun updateDate(date: String) {
        _selectedDate.value = date
    }

    fun updateTime(time: String) {
        _selectedTime.value = time
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            val currentItem = cartItems.value.find { it.productId == productId }
            if (currentItem != null) {
                repository.updateCartItem(currentItem.copy(quantity = newQuantity))
            }
        }
    }

    // Place Order Logic
    fun placeOrder(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val date = _selectedDate.value
        val time = _selectedTime.value
        val branch = _selectedBranch.value
        val items = cartItems.value
        val total = totalAmount.value

        // Validation
        if (date.isEmpty() || time.isEmpty()) {
            onError("Please select pickup date and time.")
            return
        }

        if (items.isEmpty()) {
            onError("Your cart is empty.")
            return
        }

        viewModelScope.launch {
            val result = repository.placeOrder(branch, date, time, items, total)
            if (result.isSuccess) {
                // Clear selections after success
                _selectedDate.value = ""
                _selectedTime.value = ""
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Order failed")
            }
        }
    }
}
