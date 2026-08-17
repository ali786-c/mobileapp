package com.cricketdraft.mobile.draft.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.draft.data.DraftApi
import com.cricketdraft.mobile.draft.data.DraftState
import com.cricketdraft.mobile.draft.data.PickRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DraftScreenState {
    data object Loading : DraftScreenState
    data class Ready(val draft: DraftState, val connection: ConnectionState = ConnectionState.Live) : DraftScreenState
    data class Error(val message: String) : DraftScreenState
}

enum class ConnectionState { Live, Reconnecting }

@HiltViewModel
class DraftViewModel @Inject constructor(
    private val draftApi: DraftApi,
) : ViewModel() {
    private val _state = MutableStateFlow<DraftScreenState>(DraftScreenState.Loading)
    val state: StateFlow<DraftScreenState> = _state.asStateFlow()
    private var pollingJob: Job? = null
    private var tournamentSlug: String? = null

    fun start(slug: String) {
        if (tournamentSlug == slug && pollingJob?.isActive == true) return
        tournamentSlug = slug
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var firstLoad = true
            while (true) {
                val result = runCatching { draftApi.state(slug).data }
                result.onSuccess { draft ->
                    _state.value = DraftScreenState.Ready(draft, ConnectionState.Live)
                    firstLoad = false
                }.onFailure { error ->
                    val current = _state.value
                    _state.value = if (current is DraftScreenState.Ready) {
                        current.copy(connection = ConnectionState.Reconnecting)
                    } else if (firstLoad) {
                        DraftScreenState.Error(friendlyError(error))
                    } else current
                }
                delay(2_000)
            }
        }
    }

    fun retry() {
        tournamentSlug?.let { start(it) }
    }

    fun pick(playerId: Long) {
        val slug = tournamentSlug ?: return
        viewModelScope.launch {
            val current = _state.value
            if (current !is DraftScreenState.Ready || !current.draft.captainCanPick) return@launch
            runCatching { draftApi.pick(slug, PickRequest(playerId)).data }
                .onSuccess { _state.value = DraftScreenState.Ready(it, ConnectionState.Live) }
                .onFailure { _state.value = current.copy(connection = ConnectionState.Reconnecting) }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}

private fun friendlyError(error: Throwable): String = when {
    error.message?.contains("401") == true -> "Your session has expired. Please sign in again."
    error.message?.contains("403") == true -> "You do not have access to this draft."
    error.message?.contains("404") == true -> "This draft is not available right now."
    else -> "We could not connect. Check your internet and try again."
}
