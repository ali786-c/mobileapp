package com.cricketdraft.mobile.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST

@Serializable
data class PlayerProfile(
    val id: Long,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val city: String? = null,
    @SerialName("playing_role") val playingRole: String,
    @SerialName("batting_style") val battingStyle: String? = null,
    @SerialName("bowling_style") val bowlingStyle: String? = null,
    val bio: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class ProfileResponse(val data: PlayerProfile?)

@Serializable
data class ProfileUpdateRequest(
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val city: String? = null,
    @SerialName("playing_role") val playingRole: String,
    @SerialName("batting_style") val battingStyle: String? = null,
    @SerialName("bowling_style") val bowlingStyle: String? = null,
    val bio: String? = null,
)

interface ProfileApi {
    @GET("profile")
    suspend fun profile(): ProfileResponse

    @PATCH("profile")
    suspend fun update(@Body request: ProfileUpdateRequest): ProfileResponse

    @GET("tournaments/{tournament}/registration")
    suspend fun registration(@Path("tournament") tournament: String): RegistrationResponse

    @POST("tournaments/{tournament}/registration")
    suspend fun register(@Path("tournament") tournament: String): RegistrationResponse
}

@Serializable
data class Registration(
    val id: Long,
    val status: String,
    @SerialName("submitted_at") val submittedAt: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
)

@Serializable
data class RegistrationResponse(val data: Registration?, val message: String? = null)
