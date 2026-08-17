package com.cricketdraft.mobile.superadmin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.superadmin.data.HealthPayload
import com.cricketdraft.mobile.superadmin.data.SuperAdminApi
import com.cricketdraft.mobile.superadmin.data.SuperAdminDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SuperAdminState {
    data object Loading : SuperAdminState
    data class Ready(val dashboard: SuperAdminDashboard, val health: HealthPayload) : SuperAdminState
    data class Error(val message: String) : SuperAdminState
}

@HiltViewModel
class SuperAdminViewModel @Inject constructor(
    private val api: SuperAdminApi,
) : ViewModel() {
    private val _state = MutableStateFlow<SuperAdminState>(SuperAdminState.Loading)
    val state: StateFlow<SuperAdminState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            runCatching {
                val dashboard = api.dashboard().data
                val health = api.health().data
                SuperAdminState.Ready(dashboard, health)
            }.onSuccess { _state.value = it }
                .onFailure { _state.value = SuperAdminState.Error(it.message ?: "Unable to load governance data") }
        }
    }
}
