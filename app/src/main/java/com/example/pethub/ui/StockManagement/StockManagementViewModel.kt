package com.example.pethub.ui.StockManagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.data.repository.AuthRepository
import com.example.pethub.data.repository.StockItem
import com.example.pethub.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockManagementViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StockUiState>(StockUiState.Loading)
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        loadStocks()
    }

    private fun loadStocks() {
        viewModelScope.launch {
            stockRepository.getBranchProductsWithDetails()
                .catch { e ->
                    _uiState.value = StockUiState.Error(e.message ?: "Unknown error")
                }
                .collect { items ->
                    _uiState.value = StockUiState.Success(items)
                }
        }
    }

    fun updateStock(documentId: String, newStock: Int) {
        viewModelScope.launch {
            stockRepository.updateStock(documentId, newStock)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSuccess()
        }
    }
}

sealed class StockUiState {
    object Loading : StockUiState()
    data class Success(val stocks: List<StockItem>) : StockUiState()
    data class Error(val message: String) : StockUiState()
}
