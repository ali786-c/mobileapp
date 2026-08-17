package com.cricketdraft.mobile.captain.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class CaptainMatchPlayer(
    val id: Long,
    @SerialName("team_id") val teamId: Long,
    @SerialName("player_name_snapshot") val name: String,
    @SerialName("player_role_snapshot") val role: String? = null,
    @SerialName("selection_type") val selectionType: String = "squad",
)

@Serializable
data class CaptainPlayingXiRequest(@SerialName("player_ids") val playerIds: List<Long>)

@Serializable
data class CaptainMatchPayload(
    val id: Long,
    val status: String,
    @SerialName("revision") val revision: Int = 0,
    val players: List<CaptainMatchPlayer> = emptyList(),
)

@Serializable
data class CaptainMatchResponse(val data: CaptainMatchPayload, val message: String? = null)

interface CaptainMatchApi {
    @POST("captain/matches/{match}/teams/{team}/playing-xi")
    suspend fun submitPlayingXi(
        @Path("match") matchId: Long,
        @Path("team") teamId: Long,
        @Body request: CaptainPlayingXiRequest,
    ): CaptainMatchResponse
}
