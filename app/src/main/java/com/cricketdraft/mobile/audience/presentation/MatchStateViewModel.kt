package com.cricketdraft.mobile.audience.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.audience.data.MatchState
import com.cricketdraft.mobile.audience.data.PublicApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MatchStateScreenState {
    data object Loading : MatchStateScreenState
    data class Ready(val match: MatchState, val connection: ConnectionState = ConnectionState.Live) : MatchStateScreenState
    data class Error(val message: String) : MatchStateScreenState
}

enum class ConnectionState { Live, Reconnecting }

@HiltViewModel
class MatchStateViewModel @Inject constructor(
    private val publicApi: PublicApi,
) : ViewModel() {
    private val _state = MutableStateFlow<MatchStateScreenState>(MatchStateScreenState.Loading)
    val state: StateFlow<MatchStateScreenState> = _state.asStateFlow()
    private var pollingJob: Job? = null

    fun start(matchId: Long) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var firstLoad = true
            while (true) {
                runCatching { publicApi.matchState(matchId).data }
                    .onSuccess { _state.value = MatchStateScreenState.Ready(it, ConnectionState.Live); firstLoad = false }
                    .onFailure {
                        val current = _state.value
                        if (current is MatchStateScreenState.Ready) _state.value = current.copy(connection = ConnectionState.Reconnecting)
                        else if (firstLoad) _state.value = MatchStateScreenState.Error("We could not load this scorecard. Check your connection and try again.")
                    }
                delay(2_000)
            }
        }
    }

    fun retry() {
        val current = _state.value
        if (current is MatchStateScreenState.Ready) start(current.match.id)
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
