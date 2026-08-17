package com.cricketdraft.mobile.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricketdraft.mobile.BuildConfig
import com.cricketdraft.mobile.auth.data.AuthApi
import com.cricketdraft.mobile.auth.data.LoginRequest
import com.cricketdraft.mobile.auth.data.UserPayload
import com.cricketdraft.mobile.core.security.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: UserPayload) : SessionState
    data class Error(val message: String) : SessionState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) : ViewModel() {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init { restoreSession() }

    fun restoreSession() {
        viewModelScope.launch {
            if (tokenStore.read().isNullOrBlank()) {
                _state.value = SessionState.SignedOut
                return@launch
            }
            runCatching { authApi.me().data }
                .onSuccess { _state.value = SessionState.SignedIn(it) }
                .onFailure {
                    tokenStore.clear()
                    _state.value = SessionState.SignedOut
                }
        }
    }

    fun login(email: String, password: String, deviceName: String = "cricket-draft-android") {
        viewModelScope.launch {
            _state.value = SessionState.Loading
            runCatching {
                authApi.login(
                    LoginRequest(
                        email = email.trim(),
                        password = password,
                        deviceName = deviceName,
                        clientSlug = BuildConfig.API_CLIENT_SLUG.takeIf { it.isNotBlank() },
                    ),
                )
            }
                .onSuccess {
                    tokenStore.save(it.token)
                    _state.value = SessionState.SignedIn(it.data)
                }
                .onFailure { _state.value = SessionState.Error(friendlyAuthError(it)) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { authApi.logout() }
            tokenStore.clear()
            _state.value = SessionState.SignedOut
        }
    }
}

private fun friendlyAuthError(error: Throwable): String {
    val message = error.message.orEmpty()
    return when {
        message.contains("401") -> "Email or password is incorrect."
        message.contains("422") -> "The API client is not configured correctly. Please contact the administrator."
        message.contains("429") -> "Too many attempts. Please wait a moment and try again."
        message.contains("Unable to resolve host") ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("CLEARTEXT", ignoreCase = true) ->
            "Unable to connect to Cricket Draft. Check your internet connection and API configuration."
        else -> "We could not sign you in. Please check your details and try again."
    }
}
