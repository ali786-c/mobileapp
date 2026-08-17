package com.cricketdraft.mobile.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.reports.data.ReportsApi
import com.cricketdraft.mobile.reports.data.ReportsPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReportsState {
    data object Loading : ReportsState
    data class Ready(val report: ReportsPayload) : ReportsState
    data class Error(val message: String) : ReportsState
}

@HiltViewModel
class ReportsViewModel @Inject constructor(private val reportsApi: ReportsApi) : ViewModel() {
    private val _state = MutableStateFlow<ReportsState>(ReportsState.Loading)
    val state: StateFlow<ReportsState> = _state.asStateFlow()
    private var slug: String? = null
    private var audience: String = "public"

    fun load(tournamentSlug: String, requestedAudience: String = "public") {
        slug = tournamentSlug
        audience = requestedAudience
        viewModelScope.launch {
            _state.value = ReportsState.Loading
            runCatching {
                when (requestedAudience) {
                    "admin" -> reportsApi.adminReport(tournamentSlug).data
                    "captain" -> reportsApi.captainReport(tournamentSlug).data
                    else -> reportsApi.publicReport(tournamentSlug).data
                }
            }.onSuccess { _state.value = ReportsState.Ready(it) }
                .onFailure { _state.value = ReportsState.Error("We could not load this report. Check your connection and try again.") }
        }
    }

    fun retry() { slug?.let { load(it, audience) } }
}
