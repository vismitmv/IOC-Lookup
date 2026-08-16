package com.example.ioclookup.data.remote.shodan

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ShodanService {
    @GET("shodan/host/{ip}")
    suspend fun getHostInfo(@Path("ip") ip: String): Response<ShodanHostResponse>

    @GET("https://internetdb.shodan.io/{ip}")
    suspend fun getInternetDbInfo(@Path("ip") ip: String): Response<ShodanInternetDbResponse>

    @GET("shodan/dns/resolve")
    suspend fun resolveDns(@Query("hostnames") hostnames: String): Response<Map<String, String>>
}

data class ShodanInternetDbResponse(
    val ip: String?,
    val ports: List<Int>?,
    val cpes: List<String>?,
    val hostnames: List<String>?,
    val tags: List<String>?,
    val vulns: List<String>?
)

// --- DTOs ---

data class ShodanHostResponse(
    @SerializedName("ip_str") val ipStr: String?,
    val ports: List<Int>?,
    val data: List<ShodanPortData>?,
    @SerializedName("country_name") val countryName: String?,
    val org: String?,
    val isp: String?,
    val os: String?,
    val hostnames: List<String>?,
    val tags: List<String>?,
    val vulns: Map<String, ShodanVuln>?
)

data class ShodanPortData(
    val port: Int?,
    val transport: String?,
    @SerializedName("_shodan") val shodan: ShodanMeta?,
    val product: String?,
    val version: String?,
    val cpe: List<String>?
)

data class ShodanMeta(
    val module: String?,
    val id: String?
)

data class ShodanVuln(
    val cvss: Double?,
    val references: List<String>?,
    val summary: String?
)
