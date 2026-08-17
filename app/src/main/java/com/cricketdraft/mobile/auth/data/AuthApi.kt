package com.cricketdraft.mobile.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("client_slug") val clientSlug: String? = null,
)

@Serializable
data class PlayerProfilePayload(
    val id: Long,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("playing_role") val playingRole: String? = null,
)

@Serializable
data class UserPayload(
    val id: Long,
    val name: String? = null,
    val email: String? = null,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    @SerialName("player_profile") val playerProfile: PlayerProfilePayload? = null,
)

@Serializable
data class LoginResponse(
    val data: UserPayload,
    val token: String,
    @SerialName("token_type") val tokenType: String,
)

@Serializable
data class UserResponse(val data: UserPayload)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): UserResponse

    @POST("auth/logout")
    suspend fun logout(): MessageResponse

    @POST("auth/logout-all")
    suspend fun logoutAll(): MessageResponse
}

@Serializable
data class MessageResponse(val message: String)
