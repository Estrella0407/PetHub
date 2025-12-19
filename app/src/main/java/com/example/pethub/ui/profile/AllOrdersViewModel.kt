package com.example.pethub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.model.OrderItem
import com.example.pethub.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AllOrdersUiState {
    object Loading : AllOrdersUiState()
    data class Success(val orders: List<OrderItem>) : AllOrdersUiState()
    data class Error(val message: String) : AllOrdersUiState()
}

@HiltViewModel
class AllOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AllOrdersUiState>(AllOrdersUiState.Loading)
    val uiState: StateFlow<AllOrdersUiState> = _uiState.asStateFlow()

    init {
        loadAllOrders()
    }

    private fun loadAllOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = AllOrdersUiState.Loading

                orderRepository.getAllOrdersForCurrentUser()
                    .map { orders ->
                        orders.map { order ->
                            orderRepository.getOrderItem(order)
                        }
                    }
                    .collect { orderItems ->
                        _uiState.value = AllOrdersUiState.Success(orderItems)
                    }
            } catch (e: Exception) {
                _uiState.value = AllOrdersUiState.Error(
                    e.message ?: "Failed to load orders"
                )
            }
        }
    }

    fun retry() {
        loadAllOrders()
    }
}

