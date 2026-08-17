package com.cricketdraft.mobile.audience.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.audience.data.FixtureSummary
import com.cricketdraft.mobile.audience.data.PublicApi
import com.cricketdraft.mobile.audience.data.StandingRow
import com.cricketdraft.mobile.audience.data.TournamentDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TournamentDetailState {
    data object Loading : TournamentDetailState
    data class Ready(
        val tournament: TournamentDetail,
        val fixtures: List<FixtureSummary>,
        val standings: List<StandingRow>,
    ) : TournamentDetailState
    data class Error(val message: String) : TournamentDetailState
}

@HiltViewModel
class TournamentDetailViewModel @Inject constructor(
    private val publicApi: PublicApi,
) : ViewModel() {
    private val _state = MutableStateFlow<TournamentDetailState>(TournamentDetailState.Loading)
    val state: StateFlow<TournamentDetailState> = _state.asStateFlow()

    fun load(slug: String) {
        viewModelScope.launch {
            _state.value = TournamentDetailState.Loading
            runCatching {
                val detail = publicApi.tournament(slug).data
                val fixtures = publicApi.fixtures(slug).data
                val standings = publicApi.standings(slug).data
                TournamentDetailState.Ready(detail, fixtures, standings)
            }.onSuccess { _state.value = it }
                .onFailure { _state.value = TournamentDetailState.Error(it.message ?: "Unable to load tournament") }
        }
    }
}
