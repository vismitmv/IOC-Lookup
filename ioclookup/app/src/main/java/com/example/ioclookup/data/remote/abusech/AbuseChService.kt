package com.example.ioclookup.data.remote.abusech

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AbuseChService {
    // --- URLhaus (URL & Domain Lookups) ---
    @FormUrlEncoded
    @POST("https://urlhaus-api.abuse.ch/v1/url/")
    suspend fun checkUrl(@Field("url") url: String): Response<UrlhausUrlResponse>

    @FormUrlEncoded
    @POST("https://urlhaus-api.abuse.ch/v1/host/")
    suspend fun checkHost(@Field("host") host: String): Response<UrlhausHostResponse>

    // --- MalwareBazaar (File Hash Lookups) ---
    @FormUrlEncoded
    @POST("https://mb-api.abuse.ch/api/v1/")
    suspend fun checkHash(
        @Field("query") query: String = "get_info",
        @Field("hash") hash: String
    ): Response<MalwareBazaarResponse>

    // --- ThreatFox (IOC / C2 Search) ---
    @FormUrlEncoded
    @POST("https://threatfox-api.abuse.ch/v1/")
    suspend fun searchIoc(
        @Field("query") query: String = "search_ioc",
        @Field("search_term") searchTerm: String
    ): Response<ThreatFoxResponse>
}

// --- DTOs ---

data class UrlhausUrlResponse(
    @SerializedName("query_status") val queryStatus: String?,
    @SerializedName("url_status") val urlStatus: String?,
    @SerializedName("threat") val threat: String?,
    val reporter: String?,
    val tags: List<String>?
)

data class UrlhausHostResponse(
    @SerializedName("query_status") val queryStatus: String?,
    val urls: List<UrlhausUrlResponse>?
)

data class MalwareBazaarResponse(
    @SerializedName("query_status") val queryStatus: String?,
    val data: List<MalwareBazaarData>?
)

data class MalwareBazaarData(
    val signature: String?,
    @SerializedName("file_type") val fileType: String?,
    @SerializedName("delivery_method") val deliveryMethod: String?,
    val reporter: String?,
    val tags: List<String>?
)

data class ThreatFoxResponse(
    @SerializedName("query_status") val queryStatus: String?,
    val data: List<ThreatFoxData>?
)

data class ThreatFoxData(
    @SerializedName("threat_type") val threatType: String?,
    @SerializedName("malware_printable") val malwarePrintable: String?,
    @SerializedName("confidence_level") val confidenceLevel: Int?,
    val reporter: String?,
    val tags: List<String>?
)
