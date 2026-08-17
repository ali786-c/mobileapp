package com.cricketdraft.mobile.reports.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class ReportTournament(
    val id: Long,
    val name: String,
    val slug: String,
    val status: String,
    @SerialName("season_name") val seasonName: String? = null,
    val city: String? = null,
    val venue: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
)

@Serializable
data class ReportSummary(
    val teams: Int = 0,
    @SerialName("registered_players") val registeredPlayers: Int = 0,
    @SerialName("approved_players") val approvedPlayers: Int = 0,
    @SerialName("selected_players") val selectedPlayers: Int = 0,
    @SerialName("total_picks") val totalPicks: Int = 0,
    @SerialName("completed_picks") val completedPicks: Int = 0,
    @SerialName("pending_picks") val pendingPicks: Int = 0,
    @SerialName("skipped_picks") val skippedPicks: Int = 0,
    val rounds: Int = 0,
    @SerialName("draft_status") val draftStatus: String = "not_configured",
)

@Serializable
data class ReportTimer(
    @SerialName("extension_count") val extensionCount: Int = 0,
    @SerialName("extended_seconds") val extendedSeconds: Int = 0,
    @SerialName("expired_picks") val expiredPicks: Int = 0,
    @SerialName("skipped_picks") val skippedPicks: Int = 0,
)

@Serializable
data class ReportPlayer(
    @SerialName("pick_number") val pickNumber: Int? = null,
    val player: String? = null,
    @SerialName("playing_role") val playingRole: String? = null,
    @SerialName("selected_at") val selectedAt: String? = null,
)

@Serializable
data class ReportSquad(
    @SerialName("team_id") val teamId: Long,
    val team: String,
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("selected_count") val selectedCount: Int = 0,
    val players: List<ReportPlayer> = emptyList(),
)

@Serializable
data class ReportHistory(
    @SerialName("pick_number") val pickNumber: Int? = null,
    val round: Int? = null,
    val team: String? = null,
    val player: String? = null,
    @SerialName("playing_role") val playingRole: String? = null,
    val status: String? = null,
    @SerialName("selected_at") val selectedAt: String? = null,
)

@Serializable
data class ReportsPayload(
    val audience: String,
    val tournament: ReportTournament,
    val summary: ReportSummary,
    @SerialName("team_squads") val teamSquads: List<ReportSquad> = emptyList(),
    val history: List<ReportHistory> = emptyList(),
    val timer: ReportTimer = ReportTimer(),
    @SerialName("captain_team") val captainTeam: String? = null,
)

@Serializable
data class ReportsResponse(val data: ReportsPayload)

interface ReportsApi {
    @GET("tournaments/{tournament}/reports")
    suspend fun publicReport(@Path("tournament") tournament: String): ReportsResponse

    @GET("tournaments/{tournament}/my-reports")
    suspend fun captainReport(@Path("tournament") tournament: String): ReportsResponse

    @GET("admin/tournaments/{tournament}/reports")
    suspend fun adminReport(@Path("tournament") tournament: String): ReportsResponse
}
