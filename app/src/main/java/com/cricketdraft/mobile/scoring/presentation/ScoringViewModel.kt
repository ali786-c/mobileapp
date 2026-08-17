package com.cricketdraft.mobile.scoring.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.scoring.data.DeliveryRequest
import com.cricketdraft.mobile.scoring.data.ScoringApi
import com.cricketdraft.mobile.scoring.data.UndoRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScoringState {
    data object Ready : ScoringState
    data object Saving : ScoringState
    data class Success(val message: String) : ScoringState
    data class Error(val message: String) : ScoringState
}

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val scoringApi: ScoringApi,
) : ViewModel() {
    private val _state = MutableStateFlow<ScoringState>(ScoringState.Ready)
    val state: StateFlow<ScoringState> = _state.asStateFlow()
    private var revision: Int? = null

    fun recordDelivery(matchId: Long, request: DeliveryRequest) {
        viewModelScope.launch {
            _state.value = ScoringState.Saving
            runCatching { scoringApi.delivery(matchId, request.copy(expectedRevision = revision)) }
                .onSuccess { response ->
                    revision = response.data.revision
                    _state.value = ScoringState.Success("Recorded ${response.data.notation}")
                }
                .onFailure { _state.value = ScoringState.Error(it.message ?: "Delivery could not be recorded") }
        }
    }

    fun startNextInnings(matchId: Long) {
        viewModelScope.launch {
            _state.value = ScoringState.Saving
            runCatching { scoringApi.nextInnings(matchId) }
                .onSuccess { _state.value = ScoringState.Success("Next innings started") }
                .onFailure { _state.value = ScoringState.Error(it.message ?: "Next innings could not be started") }
        }
    }

    fun undo(matchId: Long, reason: String) {
        viewModelScope.launch {
            _state.value = ScoringState.Saving
            runCatching { scoringApi.undo(matchId, UndoRequest(reason)) }
                .onSuccess { _state.value = ScoringState.Success(it.message) }
                .onFailure { _state.value = ScoringState.Error(it.message ?: "Delivery could not be undone") }
        }
    }
}
