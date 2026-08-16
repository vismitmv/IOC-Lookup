package com.example.ioclookup.domain.model

import com.google.gson.JsonObject

/**
 * Result from a single threat intelligence source.
 */
sealed class SourceResult {
    abstract val rawJson: String?
    abstract val isLoading: Boolean
    abstract val error: String?

    data class VirusTotal(
        val detectionCount: Int = 0,
        val totalEngines: Int = 0,
        val categories: List<String> = emptyList(),
        val tags: List<String> = emptyList(),
        val reputation: Int = 0,
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String? = null
    ) : SourceResult() {
        val detectionRatio: Double
            get() = if (totalEngines > 0) detectionCount.toDouble() / totalEngines else 0.0
        val detectionLabel: String
            get() = "$detectionCount / $totalEngines"
    }

    data class AbuseIPDB(
        val abuseConfidenceScore: Int = 0,
        val totalReports: Int = 0,
        val numDistinctUsers: Int = 0,
        val lastReportedAt: String? = null,
        val countryCode: String? = null,
        val isp: String? = null,
        val usageType: String? = null,
        val isTor: Boolean = false,
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String? = null
    ) : SourceResult()

    data class Shodan(
        val ports: List<Int> = emptyList(),
        val services: List<ShodanService> = emptyList(),
        val country: String? = null,
        val org: String? = null,
        val isp: String? = null,
        val cves: List<String> = emptyList(),
        val hostnames: List<String> = emptyList(),
        val os: String? = null,
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String? = null
    ) : SourceResult()

    data class OTX(
        val pulseCount: Int = 0,
        val tags: List<String> = emptyList(),
        val malwareFamilies: List<String> = emptyList(),
        val adversaries: List<String> = emptyList(),
        val industries: List<String> = emptyList(),
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String? = null
    ) : SourceResult()

    data class AbuseCh(
        val isFlagged: Boolean = false,
        val status: String? = null,
        val threatType: String? = null,
        val signature: String? = null,
        val reporter: String? = null,
        val tags: List<String> = emptyList(),
        val confidenceLevel: Int = 0,
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String? = null
    ) : SourceResult()

    data class CustomFeed(
        val feedName: String,
        val isFlagged: Boolean = false,
        val summary: String? = null,
        val responseCode: Int = 200,
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String? = null
    ) : SourceResult()

    data class Loading(
        val sourceName: String,
        override val rawJson: String? = null,
        override val isLoading: Boolean = true,
        override val error: String? = null
    ) : SourceResult()

    data class Error(
        val sourceName: String,
        override val rawJson: String? = null,
        override val isLoading: Boolean = false,
        override val error: String
    ) : SourceResult()
}

data class ShodanService(
    val port: Int,
    val transport: String,
    val product: String? = null,
    val version: String? = null
)
