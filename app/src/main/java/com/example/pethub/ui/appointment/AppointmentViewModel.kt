package com.example.pethub.ui.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pethub.R
import com.example.pethub.data.model.Branch
import com.example.pethub.data.model.BranchService
import com.example.pethub.data.repository.BranchRepository
import com.example.pethub.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// UI State for the entire Appointment Screen
data class AppointmentUiState(
    val serviceName: String = "",
    val serviceImageRes: Int = 0,
    val availableBranches: List<Branch> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val branchRepository: BranchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serviceId: String = savedStateHandle.get<String>("serviceId") ?: ""

    val uiState: StateFlow<AppointmentUiState> = branchRepository.listenToBranches().flatMapLatest { branches ->
        if (branches.isEmpty()) {
            flowOf(createInitialState(serviceId).copy(isLoading = false))
        } else {
            val availabilityFlows: List<Flow<Pair<Branch, Boolean>>> = branches.map { branch ->
                if (branch.branchId.isEmpty()) {
                    flowOf(branch to false)
                } else {
                    serviceRepository.listenToBranchServiceAvailability(branch.branchId, serviceId)
                        .map { branchService: BranchService? -> branch to (branchService?.availability ?: false) }
                }
            }
            combine(availabilityFlows) { availabilityPairs ->
                val availableBranches = availabilityPairs.filter { it.second }.map { it.first }
                createInitialState(serviceId).copy(
                    availableBranches = availableBranches,
                    isLoading = false
                )
            }
        }
    }.catch { e ->
        // If any error occurs in the flow, emit an error state
        emit(AppointmentUiState(isLoading = false, error = e.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppointmentUiState(isLoading = true) // Start in a loading state
    )

    private fun createInitialState(serviceId: String): AppointmentUiState {
        return when (serviceId) {
            "grooming" -> AppointmentUiState(serviceName = "Grooming", serviceImageRes = R.drawable.grooming_nobg)
            "boarding" -> AppointmentUiState(serviceName = "Boarding", serviceImageRes = R.drawable.boarding_nobg)
            "walking" -> AppointmentUiState(serviceName = "Walking", serviceImageRes = R.drawable.walking_nobg)
            "daycare" -> AppointmentUiState(serviceName = "Daycare", serviceImageRes = R.drawable.daycare_nobg)
            "training" -> AppointmentUiState(serviceName = "Training", serviceImageRes = R.drawable.training_nobg)
            else -> AppointmentUiState(serviceName = "Service")
        }
    }
}
