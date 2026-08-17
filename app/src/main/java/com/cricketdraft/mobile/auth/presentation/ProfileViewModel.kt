package com.cricketdraft.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.auth.data.PlayerProfile
import com.cricketdraft.mobile.auth.data.ProfileApi
import com.cricketdraft.mobile.auth.data.ProfileUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileState {
    data object Loading : ProfileState
    data object Empty : ProfileState
    data class Ready(val profile: PlayerProfile) : ProfileState
    data class Error(val message: String) : ProfileState
    data class Saved(val profile: PlayerProfile) : ProfileState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileApi: ProfileApi,
) : ViewModel() {
    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            runCatching { profileApi.profile().data }
                .onSuccess { _state.value = it?.let(ProfileState::Ready) ?: ProfileState.Empty }
                .onFailure { _state.value = ProfileState.Error(it.message ?: "Unable to load profile") }
        }
    }

    fun save(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            _state.value = ProfileState.Loading
            runCatching { profileApi.update(request).data }
                .onSuccess { profile ->
                    _state.value = profile?.let { ProfileState.Saved(it) } ?: ProfileState.Error("Profile response was empty")
                }
                .onFailure { _state.value = ProfileState.Error(it.message ?: "Unable to save profile") }
        }
    }
}
