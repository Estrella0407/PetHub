package com.example.pethub.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.Order
import com.example.pethub.data.model.ProductOrder
import com.example.pethub.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State for the Order Detail Screen
sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val order: Order, val productOrders: List<ProductOrder>) : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val orderId: String = savedStateHandle.get<String>("orderId")!!

    init {
        if (orderId.isNotBlank()) {
            loadOrderDetails()
        } else {
            _uiState.value = OrderDetailUiState.Error("Order ID is missing.")
        }
    }

    fun loadOrderDetails() {
        viewModelScope.launch {
            _uiState.value = OrderDetailUiState.Loading
            try {
                // Fetch the main order document
                val orderResult = orderRepository.getOrderDetail(orderId)
                val order = orderResult.getOrNull()

                if (order == null) {
                    _uiState.value = OrderDetailUiState.Error("Order not found.")
                    return@launch
                }

                // Fetch the list of products associated with this order
                val productOrdersResult = orderRepository.getProductOrdersForOrder(orderId)
                val productOrders = productOrdersResult.getOrNull() ?: emptyList()

                _uiState.value = OrderDetailUiState.Success(order, productOrders)

            } catch (e: Exception) {
                _uiState.value = OrderDetailUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}
