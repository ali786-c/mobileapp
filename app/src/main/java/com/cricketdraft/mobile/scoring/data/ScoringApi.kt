package com.cricketdraft.mobile.scoring.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class WicketRequest(
    @SerialName("dismissed_player_id") val dismissedPlayerId: Long? = null,
    @SerialName("dismissal_type") val dismissalType: String? = null,
    @SerialName("fielder_id") val fielderId: Long? = null,
    @SerialName("runs_completed") val runsCompleted: Int? = null,
    val notes: String? = null,
)

@Serializable
data class DeliveryRequest(
    @SerialName("striker_id") val strikerId: Long,
    @SerialName("non_striker_id") val nonStrikerId: Long,
    @SerialName("bowler_id") val bowlerId: Long,
    @SerialName("runs_off_bat") val runsOffBat: Int = 0,
    val wides: Int = 0,
    @SerialName("no_balls") val noBalls: Int = 0,
    val byes: Int = 0,
    @SerialName("leg_byes") val legByes: Int = 0,
    @SerialName("penalty_runs") val penaltyRuns: Int = 0,
    val commentary: String? = null,
    @SerialName("expected_revision") val expectedRevision: Int? = null,
    val wicket: WicketRequest? = null,
)

@Serializable
data class DeliveryResponse(
    @SerialName("delivery_id") val deliveryId: Long,
    val revision: Int,
    val notation: String,
)

@Serializable
data class DeliveryApiResponse(val data: DeliveryResponse)

@Serializable
data class NextInningsResponse(@SerialName("innings_id") val inningsId: Long, @SerialName("match_id") val matchId: Long)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class UndoRequest(val reason: String)

interface ScoringApi {
    @POST("matches/{match}/deliveries")
    suspend fun delivery(@Path("match") match: Long, @Body request: DeliveryRequest): DeliveryApiResponse

    @POST("matches/{match}/next-innings")
    suspend fun nextInnings(@Path("match") match: Long): ApiResponse<NextInningsResponse>

    @POST("matches/{match}/undo")
    suspend fun undo(@Path("match") match: Long, @Body request: UndoRequest): MessageResponse
}

@Serializable
data class ApiResponse<T>(val data: T)
