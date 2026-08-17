package com.cricketdraft.mobile.audience.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class ApiCollection<T>(
    val data: List<T> = emptyList(),
    val meta: PaginationMeta? = null,
    val links: PaginationLinks? = null,
)

@Serializable
data class PaginationMeta(
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
    val total: Int? = null,
)

@Serializable
data class PaginationLinks(
    val first: String? = null,
    val last: String? = null,
    val prev: String? = null,
    val next: String? = null,
)

@Serializable
data class TournamentSummary(
    val id: Long,
    val name: String,
    val slug: String,
    val status: String,
    val description: String? = null,
    val location: String? = null,
    val city: String? = null,
    val timezone: String? = null,
    @SerialName("season_name") val seasonName: String? = null,
    @SerialName("is_public") val isPublic: Boolean = true,
)

@Serializable
data class TeamSummary(
    val id: Long,
    val name: String,
    @SerialName("short_name") val shortName: String? = null,
)

@Serializable
data class FixtureSummary(
    val id: Long,
    @SerialName("round_number") val roundNumber: Int? = null,
    @SerialName("round_name") val roundName: String? = null,
    @SerialName("match_number") val matchNumber: Int? = null,
    @SerialName("scheduled_at") val scheduledAt: String? = null,
    val timezone: String? = null,
    val venue: String? = null,
    val city: String? = null,
    val status: String,
    @SerialName("home_team") val homeTeam: TeamSummary,
    @SerialName("away_team") val awayTeam: TeamSummary,
    @SerialName("match_id") val matchId: Long? = null,
    @SerialName("match_status") val matchStatus: String? = null,
)

@Serializable
data class StandingRow(
    val position: Int? = null,
    val team: TeamSummary,
    val played: Int,
    val wins: Int,
    val losses: Int,
    val ties: Int,
    @SerialName("no_results") val noResults: Int,
    val points: Int,
    @SerialName("net_run_rate") val netRunRate: Double,
)

@Serializable
data class TournamentDetail(
    val id: Long,
    val name: String,
    val slug: String,
    val status: String,
    val description: String? = null,
    val location: String? = null,
    val city: String? = null,
    val timezone: String? = null,
    @SerialName("default_overs_per_innings") val defaultOversPerInnings: Int? = null,
)

interface PublicApi {
    @GET("tournaments")
    suspend fun tournaments(@Query("page") page: Int = 1): ApiCollection<TournamentSummary>

    @GET("tournaments/{tournament}")
    suspend fun tournament(@Path("tournament") tournament: String): ApiResponse<TournamentDetail>

    @GET("tournaments/{tournament}/fixtures")
    suspend fun fixtures(@Path("tournament") tournament: String): ApiResponse<List<FixtureSummary>>

    @GET("tournaments/{tournament}/standings")
    suspend fun standings(@Path("tournament") tournament: String): ApiResponse<List<StandingRow>>

    @GET("matches/{match}/state")
    suspend fun matchState(@Path("match") match: Long): ApiResponse<MatchState>
}

@Serializable
data class MatchInningsSummary(
    val id: Long,
    val number: Int,
    @SerialName("batting_team") val battingTeam: TeamSummary,
    @SerialName("bowling_team") val bowlingTeam: TeamSummary,
    val runs: Int,
    val wickets: Int,
    @SerialName("legal_balls") val legalBalls: Int,
    @SerialName("maximum_overs") val maximumOvers: Int,
    val overs: String,
    val target: Int? = null,
    val status: String,
)

@Serializable
data class MatchState(
    val id: Long,
    val revision: Int,
    val status: String,
    @SerialName("result_type") val resultType: String? = null,
    @SerialName("result_summary") val resultSummary: String? = null,
    @SerialName("winner_team_id") val winnerTeamId: Long? = null,
    @SerialName("overs_per_innings") val oversPerInnings: Int? = null,
    val innings: List<MatchInningsSummary> = emptyList(),
)

@Serializable
data class ApiResponse<T>(val data: T)
