package com.cricketdraft.mobile.admin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class AdminTournament(
    val id: Long,
    val name: String,
    val slug: String,
    val status: String,
    @SerialName("season_name") val seasonName: String? = null,
    @SerialName("teams_count") val teamsCount: Int = 0,
    @SerialName("tournament_players_count") val playersCount: Int = 0,
    @SerialName("fixtures_count") val fixturesCount: Int = 0,
    @SerialName("matches_count") val matchesCount: Int = 0,
)

@Serializable
data class AdminTournamentPage(
    val data: List<AdminTournament> = emptyList(),
    val meta: AdminPagination? = null,
)

@Serializable
data class AdminTournamentPageResponse(val data: AdminTournamentPage)

@Serializable
data class AdminPagination(
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    val total: Int? = null,
)

@Serializable
data class StatusRequest(val status: String)

@Serializable
data class AdminTournamentResponse(val data: AdminTournament, val message: String? = null)

interface AdminApi {
    @GET("admin/tournaments")
    suspend fun tournaments(): AdminTournamentPageResponse

    @POST("admin/tournaments/{tournament}/status")
    suspend fun status(@Path("tournament") tournament: String, @Body request: StatusRequest): AdminTournamentResponse
}
