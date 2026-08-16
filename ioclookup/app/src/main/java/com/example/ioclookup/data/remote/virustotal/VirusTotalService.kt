package com.example.ioclookup.data.remote.virustotal

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.Response

interface VirusTotalService {
    @GET("api/v3/ip_addresses/{ip}")
    suspend fun getIpReport(@Path("ip") ip: String): Response<VtIpResponse>

    @GET("api/v3/domains/{domain}")
    suspend fun getDomainReport(@Path("domain") domain: String): Response<VtDomainResponse>

    @GET("api/v3/files/{hash}")
    suspend fun getFileReport(@Path("hash") hash: String): Response<VtFileResponse>

    @FormUrlEncoded
    @POST("api/v3/urls")
    suspend fun submitUrl(@Field("url") url: String): Response<VtUrlSubmitResponse>

    @GET("api/v3/analyses/{id}")
    suspend fun getUrlAnalysis(@Path("id") id: String): Response<VtAnalysisResponse>

    @GET("api/v3/urls/{id}")
    suspend fun getUrlReport(@Path("id") id: String): Response<VtDomainResponse>
}

// --- DTOs ---

data class VtIpResponse(
    val data: VtIpData?
)

data class VtIpData(
    val attributes: VtIpAttributes?
)

data class VtIpAttributes(
    @SerializedName("last_analysis_stats") val stats: VtAnalysisStats?,
    val categories: Map<String, String>?,
    val tags: List<String>?,
    val reputation: Int?,
    @SerializedName("as_owner") val asOwner: String?,
    val country: String?,
    val network: String?
)

data class VtDomainResponse(
    val data: VtDomainData?
)

data class VtDomainData(
    val attributes: VtDomainAttributes?
)

data class VtDomainAttributes(
    @SerializedName("last_analysis_stats") val stats: VtAnalysisStats?,
    val categories: Map<String, String>?,
    val tags: List<String>?,
    val reputation: Int?,
    val registrar: String?,
    @SerializedName("creation_date") val creationDate: Long?
)

data class VtFileResponse(
    val data: VtFileData?
)

data class VtFileData(
    val attributes: VtFileAttributes?
)

data class VtFileAttributes(
    @SerializedName("last_analysis_stats") val stats: VtAnalysisStats?,
    val tags: List<String>?,
    val reputation: Int?,
    @SerializedName("meaningful_name") val meaningfulName: String?,
    @SerializedName("type_description") val typeDescription: String?,
    val names: List<String>?
)

data class VtAnalysisStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val undetected: Int = 0,
    val harmless: Int = 0,
    val timeout: Int = 0
) {
    val total: Int get() = malicious + suspicious + undetected + harmless + timeout
}

data class VtUrlSubmitResponse(
    val data: VtUrlSubmitData?
)

data class VtUrlSubmitData(
    val id: String?
)

data class VtAnalysisResponse(
    val data: VtAnalysisData?
)

data class VtAnalysisData(
    val attributes: VtAnalysisAttributes?
)

data class VtAnalysisAttributes(
    val stats: VtAnalysisStats?,
    val status: String?
)
