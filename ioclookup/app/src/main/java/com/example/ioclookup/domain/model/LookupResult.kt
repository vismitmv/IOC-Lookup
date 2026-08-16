package com.example.ioclookup.domain.model

/**
 * The complete result of an IOC lookup, aggregating results from all queried sources.
 */
data class LookupResult(
    val id: Long = 0L,
    val ioc: String,
    val iocType: IocType,
    val verdict: Verdict,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: Map<String, SourceResult> = emptyMap(),
    val isBookmarked: Boolean = false,
    val bookmarkNote: String = "",
    val isFromCache: Boolean = false
) {
    val vtResult: SourceResult.VirusTotal?
        get() = sources["virustotal"] as? SourceResult.VirusTotal

    val abuseResult: SourceResult.AbuseIPDB?
        get() = sources["abuseipdb"] as? SourceResult.AbuseIPDB

    val shodanResult: SourceResult.Shodan?
        get() = sources["shodan"] as? SourceResult.Shodan

    val otxResult: SourceResult.OTX?
        get() = sources["otx"] as? SourceResult.OTX
}
