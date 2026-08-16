package com.example.ioclookup.data.remote.abuseipdb

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AbuseIPDBService {
    @GET("api/v2/check")
    suspend fun checkIp(
        @Query("ipAddress") ipAddress: String,
        @Query("maxAgeInDays") maxAgeInDays: Int = 90,
        @Query("verbose") verbose: Boolean = true
    ): Response<AbuseCheckResponse>
}

// --- DTOs ---

data class AbuseCheckResponse(
    val data: AbuseCheckData?
)

data class AbuseCheckData(
    @SerializedName("ipAddress") val ipAddress: String?,
    @SerializedName("isPublic") val isPublic: Boolean?,
    @SerializedName("ipVersion") val ipVersion: Int?,
    @SerializedName("isWhitelisted") val isWhitelisted: Boolean?,
    @SerializedName("abuseConfidenceScore") val abuseConfidenceScore: Int?,
    @SerializedName("countryCode") val countryCode: String?,
    @SerializedName("usageType") val usageType: String?,
    @SerializedName("isp") val isp: String?,
    @SerializedName("domain") val domain: String?,
    @SerializedName("isTor") val isTor: Boolean?,
    @SerializedName("totalReports") val totalReports: Int?,
    @SerializedName("numDistinctUsers") val numDistinctUsers: Int?,
    @SerializedName("lastReportedAt") val lastReportedAt: String?
)
