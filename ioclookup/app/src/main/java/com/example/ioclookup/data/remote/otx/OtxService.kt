package com.example.ioclookup.data.remote.otx

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OtxService {
    @GET("api/v1/indicators/IPv4/{ip}/general")
    suspend fun getIpGeneral(@Path("ip") ip: String): Response<OtxGeneralResponse>

    @GET("api/v1/indicators/IPv6/{ip}/general")
    suspend fun getIpv6General(@Path("ip") ip: String): Response<OtxGeneralResponse>

    @GET("api/v1/indicators/domain/{domain}/general")
    suspend fun getDomainGeneral(@Path("domain") domain: String): Response<OtxGeneralResponse>

    @GET("api/v1/indicators/hostname/{hostname}/general")
    suspend fun getHostnameGeneral(@Path("hostname") hostname: String): Response<OtxGeneralResponse>

    @GET("api/v1/indicators/url/general")
    suspend fun getUrlGeneral(@Query("url") url: String): Response<OtxGeneralResponse>

    @GET("api/v1/indicators/file/{hash}/general")
    suspend fun getFileGeneral(@Path("hash") hash: String): Response<OtxGeneralResponse>
}

// --- DTOs ---

data class OtxGeneralResponse(
    @SerializedName("pulse_info") val pulseInfo: OtxPulseInfo?,
    val tags: List<String>?,
    val type: String?,
    val indicator: String?
)

data class OtxPulseInfo(
    val count: Int?,
    val pulses: List<OtxPulse>?,
    val references: List<String>?
)

data class OtxPulse(
    val id: String?,
    val name: String?,
    val tags: List<String>?,
    @SerializedName("malware_families") val malwareFamilies: List<OtxMalwareFamily>?,
    @SerializedName("adversary") val adversary: String?,
    @SerializedName("industries") val industries: List<String>?,
    @SerializedName("TLP") val tlp: String?
)

data class OtxMalwareFamily(
    val id: String?
)
