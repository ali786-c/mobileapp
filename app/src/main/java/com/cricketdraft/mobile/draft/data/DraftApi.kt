package com.cricketdraft.mobile.draft.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class DraftTeam(
    val id: Long,
    val name: String,
    @SerialName("short_name") val shortName: String? = null,
)

@Serializable
data class DraftPlayer(
    val id: Long,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("playing_role") val playingRole: String? = null,
)

@Serializable
data class DraftPick(
    @SerialName("pick_number") val pickNumber: Int,
    val round: Int? = null,
    val status: String,
    val team: DraftTeam? = null,
    val player: DraftPlayer? = null,
)

@Serializable
data class DraftTimer(
    @SerialName("remaining_seconds") val remainingSeconds: Int? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("server_now") val serverNow: String,
    val duration: Int? = null,
    val expired: Boolean = false,
)

@Serializable
data class TeamSquad(
    val id: Long,
    val name: String,
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("selected_count") val selectedCount: Int,
    @SerialName("selected_players") val selectedPlayers: List<SelectedPlayer> = emptyList(),
)

@Serializable
data class SelectedPlayer(
    @SerialName("pick_number") val pickNumber: Int,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("playing_role") val playingRole: String? = null,
    @SerialName("selected_at") val selectedAt: String? = null,
)

@Serializable
data class DraftSummary(
    val total: Int,
    val selected: Int,
    val active: Int,
    val expired: Int,
    val skipped: Int,
    val pending: Int,
)

@Serializable
data class DraftState(
    val id: Long,
    val status: String,
    val revision: Int,
    @SerialName("current_pick_number") val currentPickNumber: Int? = null,
    @SerialName("current_round") val currentRound: Int? = null,
    @SerialName("current_team") val currentTeam: DraftTeam? = null,
    @SerialName("captain_team") val captainTeam: DraftTeam? = null,
    @SerialName("captain_can_pick") val captainCanPick: Boolean = false,
    val timer: DraftTimer,
    val summary: DraftSummary,
    @SerialName("team_squads") val teamSquads: List<TeamSquad> = emptyList(),
    @SerialName("available_players") val availablePlayers: List<DraftPlayer> = emptyList(),
    val picks: List<DraftPick> = emptyList(),
)

@Serializable
data class PickRequest(@SerialName("tournament_player_id") val tournamentPlayerId: Long)

@Serializable
data class DraftResponse(val data: DraftState)

interface DraftApi {
    @GET("tournaments/{tournament}/draft/state")
    suspend fun state(@Path("tournament") tournament: String): DraftResponse

    @POST("tournaments/{tournament}/draft/pick")
    suspend fun pick(@Path("tournament") tournament: String, @Body request: PickRequest): DraftResponse
}
