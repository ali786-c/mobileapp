package com.cricketdraft.mobile.superadmin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class SuperAdminDashboard(
    val users: Int,
    val roles: Map<String, Int> = emptyMap(),
    val tournaments: Int,
    @SerialName("live_matches") val liveMatches: Int,
    @SerialName("pending_registrations") val pendingRegistrations: Int,
    @SerialName("api_clients") val apiClients: Int,
    @SerialName("active_api_clients") val activeApiClients: Int,
    @SerialName("active_sessions") val activeSessions: Int,
)

@Serializable
data class HealthPayload(
    val application: String,
    val database: String,
    @SerialName("database_response_ms") val databaseResponseMs: Double? = null,
    val scheduler: String,
    val api: String,
    val storage: String,
)

@Serializable
data class DashboardResponse(val data: SuperAdminDashboard)
@Serializable
data class HealthResponse(val data: HealthPayload)

interface SuperAdminApi {
    @GET("super-admin/dashboard")
    suspend fun dashboard(): DashboardResponse

    @GET("super-admin/health")
    suspend fun health(): HealthResponse
}
