package com.cricketdraft.mobile.captain.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.captain.data.CaptainMatchApi
import com.cricketdraft.mobile.captain.data.CaptainMatchPayload
import com.cricketdraft.mobile.captain.data.CaptainPlayingXiRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlayingXiState {
    data object Idle : PlayingXiState
    data object Saving : PlayingXiState
    data class Saved(val match: CaptainMatchPayload) : PlayingXiState
    data class Error(val message: String) : PlayingXiState
}

@HiltViewModel
class PlayingXiViewModel @Inject constructor(private val api: CaptainMatchApi) : ViewModel() {
    private val _state = MutableStateFlow<PlayingXiState>(PlayingXiState.Idle)
    val state: StateFlow<PlayingXiState> = _state.asStateFlow()

    fun submit(matchId: Long, teamId: Long, playerIds: List<Long>) {
        if (playerIds.isEmpty()) {
            _state.value = PlayingXiState.Error("Select your players before submitting the XI.")
            return
        }
        viewModelScope.launch {
            _state.value = PlayingXiState.Saving
            runCatching { api.submitPlayingXi(matchId, teamId, CaptainPlayingXiRequest(playerIds)).data }
                .onSuccess { _state.value = PlayingXiState.Saved(it) }
                .onFailure { _state.value = PlayingXiState.Error("We could not submit the XI. Check your connection and try again.") }
        }
    }
}
