package com.cricketdraft.mobile.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.admin.data.AdminApi
import com.cricketdraft.mobile.admin.data.AdminTournament
import com.cricketdraft.mobile.admin.data.StatusRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminTournamentState {
    data object Loading : AdminTournamentState
    data class Ready(val tournaments: List<AdminTournament>) : AdminTournamentState
    data class Error(val message: String) : AdminTournamentState
}

@HiltViewModel
class AdminTournamentViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {
    private val _state = MutableStateFlow<AdminTournamentState>(AdminTournamentState.Loading)
    val state: StateFlow<AdminTournamentState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            runCatching { adminApi.tournaments().data.data }
                .onSuccess { _state.value = AdminTournamentState.Ready(it) }
                .onFailure { _state.value = AdminTournamentState.Error(it.message ?: "Unable to load admin tournaments") }
        }
    }

    fun changeStatus(slug: String, status: String) {
        viewModelScope.launch {
            runCatching { adminApi.status(slug, StatusRequest(status)) }
                .onSuccess { load() }
                .onFailure { _state.value = AdminTournamentState.Error(it.message ?: "Status change failed") }
        }
    }
}
