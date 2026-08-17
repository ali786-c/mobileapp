package com.cricketdraft.mobile.audience.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.audience.data.PublicApi
import com.cricketdraft.mobile.audience.data.TournamentSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TournamentListState {
    data object Loading : TournamentListState
    data class Ready(val tournaments: List<TournamentSummary>, val page: Int, val lastPage: Int?) : TournamentListState
    data class Error(val message: String) : TournamentListState
}

@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val publicApi: PublicApi,
) : ViewModel() {
    private val _state = MutableStateFlow<TournamentListState>(TournamentListState.Loading)
    val state: StateFlow<TournamentListState> = _state.asStateFlow()

    init { load() }

    fun load(page: Int = 1) {
        viewModelScope.launch {
            if (_state.value is TournamentListState.Ready) {
                val existing = _state.value as TournamentListState.Ready
                _state.value = existing.copy(page = page)
            } else {
                _state.value = TournamentListState.Loading
            }
            runCatching { publicApi.tournaments(page) }
                .onSuccess { response ->
                    _state.value = TournamentListState.Ready(
                        tournaments = response.data,
                        page = response.meta?.currentPage ?: page,
                        lastPage = response.meta?.lastPage,
                    )
                }
                .onFailure { _state.value = TournamentListState.Error(it.message ?: "Unable to load tournaments") }
        }
    }
}
