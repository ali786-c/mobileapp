package com.cricketdraft.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.auth.data.ProfileApi
import com.cricketdraft.mobile.auth.data.Registration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegistrationState {
    data object Loading : RegistrationState
    data object NotRegistered : RegistrationState
    data class Registered(val registration: Registration) : RegistrationState
    data class Error(val message: String) : RegistrationState
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val profileApi: ProfileApi,
) : ViewModel() {
    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Loading)
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun load(tournamentSlug: String) {
        viewModelScope.launch {
            runCatching { profileApi.registration(tournamentSlug).data }
                .onSuccess { _state.value = it?.let(RegistrationState::Registered) ?: RegistrationState.NotRegistered }
                .onFailure { _state.value = RegistrationState.Error(it.message ?: "Unable to load registration") }
        }
    }

    fun submit(tournamentSlug: String) {
        viewModelScope.launch {
            _state.value = RegistrationState.Loading
            runCatching { profileApi.register(tournamentSlug).data }
                .onSuccess { _state.value = it?.let(RegistrationState::Registered) ?: RegistrationState.Error("Registration response was empty") }
                .onFailure { _state.value = RegistrationState.Error(it.message ?: "Unable to submit registration") }
        }
    }
}
