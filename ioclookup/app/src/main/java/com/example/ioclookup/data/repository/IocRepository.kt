package com.example.ioclookup.data.repository

import com.example.ioclookup.data.local.dao.LookupDao
import com.example.ioclookup.data.local.entity.LookupEntity
import com.example.ioclookup.data.remote.abuseipdb.AbuseIPDBService
import com.example.ioclookup.data.remote.otx.OtxService
import com.example.ioclookup.data.remote.shodan.ShodanService
import com.example.ioclookup.data.remote.virustotal.VirusTotalService
import com.example.ioclookup.data.security.SecurePreferences
import com.example.ioclookup.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import com.example.ioclookup.data.local.dao.CustomFeedDao
import com.example.ioclookup.data.remote.abusech.AbuseChService
import com.example.ioclookup.data.repository.CustomFeedRepository

@Singleton
class IocRepository @Inject constructor(
    private val lookupDao: LookupDao,
    private val customFeedDao: CustomFeedDao,
    private val customFeedRepository: CustomFeedRepository,
    private val blocklistRepository: BlocklistRepository,
    private val vtService: VirusTotalService,
    private val abuseService: AbuseIPDBService,
    private val shodanService: ShodanService,
    private val otxService: OtxService,
    private val abuseChService: AbuseChService,
    private val prefs: SecurePreferences,
    private val gson: Gson
) {
    // ─── History & Bookmarks ────────────────────────────────────────────────

    fun getAllLookups(): Flow<List<LookupResult>> =
        lookupDao.getAllLookups().map { entities -> entities.map { it.toDomain() } }

    fun getBookmarks(): Flow<List<LookupResult>> =
        lookupDao.getBookmarks().map { entities -> entities.map { it.toDomain() } }

    fun searchLookups(query: String, type: String, verdict: String): Flow<List<LookupResult>> =
        lookupDao.searchLookups(query, type, verdict).map { entities -> entities.map { it.toDomain() } }

    suspend fun deleteById(id: Long) = lookupDao.deleteById(id)

    suspend fun clearAll() = lookupDao.clearAll()

    suspend fun updateBookmark(id: Long, isBookmarked: Boolean, note: String) =
        lookupDao.updateBookmark(id, isBookmarked, note)

    // ─── Lookup ─────────────────────────────────────────────────────────────

    /**
     * Performs an IOC lookup, checking cache first.
     * If cache is fresh (within TTL), returns cached result.
     * Otherwise, queries all applicable sources in parallel.
     */
    suspend fun lookup(ioc: String, iocType: IocType, forceRefresh: Boolean = false): LookupResult {
        if (!forceRefresh) {
            val cached = lookupDao.getLatestByIoc(ioc)
            if (cached != null) {
                val ageMs = System.currentTimeMillis() - cached.timestamp
                val ttlMs = prefs.cacheTtlHours * 3_600_000L
                if (ageMs < ttlMs) {
                    return cached.toDomain().copy(isFromCache = true)
                }
            }
        }

        val sources = mutableMapOf<String, SourceResult>()

        coroutineScope {
            // ── VirusTotal ───────────────────────────────────────────────────
            val vtDeferred = if (prefs.vtEnabled && prefs.vtApiKey.isNotBlank()) {
                async { fetchVirusTotal(ioc, iocType) }
            } else null

            // ── AbuseIPDB ────────────────────────────────────────────────────
            val abuseDeferred = if (prefs.abuseEnabled && prefs.abuseApiKey.isNotBlank()
                && iocType.isIp) {
                async { fetchAbuseIPDB(ioc) }
            } else null

            // ── Shodan ───────────────────────────────────────────────────────
            val shodanDeferred = if (prefs.shodanEnabled && (iocType.isIp || iocType == IocType.DOMAIN)) {
                async { fetchShodan(ioc, iocType) }
            } else null

            // ── OTX ──────────────────────────────────────────────────────────
            val otxDeferred = if (prefs.otxEnabled && prefs.otxApiKey.isNotBlank()) {
                async { fetchOtx(ioc, iocType) }
            } else null

            // ── abuse.ch ──────────────────────────────────────────────────────
            val abuseChDeferred = async { fetchAbuseCh(ioc, iocType) }

            // ── Custom Feeds ──────────────────────────────────────────────────
            val activeCustomFeeds = customFeedDao.getActiveFeeds()
            val customFeedDeferreds = activeCustomFeeds.map { feed ->
                async { customFeedRepository.executeCustomFeed(feed, ioc) }
            }

            // ── Local Firewall Blocklists ────────────────────────────────────
            val blocklistDeferred = async { blocklistRepository.checkBlocklists(ioc) }

            vtDeferred?.await()?.let { sources["virustotal"] = it }
            abuseDeferred?.await()?.let { sources["abuseipdb"] = it }
            shodanDeferred?.await()?.let { sources["shodan"] = it }
            otxDeferred?.await()?.let { sources["otx"] = it }
            abuseChDeferred.await().let { sources["abusech"] = it }

            customFeedDeferreds.forEachIndexed { index, deferred ->
                val feedName = activeCustomFeeds.getOrNull(index)?.name ?: "custom_feed_$index"
                sources["custom_$feedName"] = deferred.await()
            }

            blocklistDeferred.await().forEach { matchResult ->
                sources["blocklist_${matchResult.feedName}"] = matchResult
            }
        }

        val vtResult = sources["virustotal"] as? SourceResult.VirusTotal
        val abuseResult = sources["abuseipdb"] as? SourceResult.AbuseIPDB
        val otxResult = sources["otx"] as? SourceResult.OTX
        val abuseChResult = sources["abusech"] as? SourceResult.AbuseCh
        val customFeedResults = sources.values.filterIsInstance<SourceResult.CustomFeed>()
        val customMaliciousCount = customFeedResults.count { it.isFlagged }

        val verdict = Verdict.fromScores(
            vtCount = vtResult?.detectionCount,
            vtRatio = vtResult?.detectionRatio,
            abuseScore = abuseResult?.abuseConfidenceScore,
            otxPulses = otxResult?.pulseCount,
            abuseChFlagged = abuseChResult?.isFlagged,
            customFeedsFlagged = customMaliciousCount
        )

        val result = LookupResult(
            ioc = ioc,
            iocType = iocType,
            verdict = verdict,
            timestamp = System.currentTimeMillis(),
            sources = sources
        )

        val id = lookupDao.insert(result.toEntity())
        return result.copy(id = id)
    }

    // ─── Source Fetchers ────────────────────────────────────────────────────

    private fun parseErrorBody(resp: retrofit2.Response<*>): String {
        val errorStr = try { resp.errorBody()?.string() } catch (e: Exception) { null }
        if (!errorStr.isNullOrBlank()) {
            try {
                val json = org.json.JSONObject(errorStr)
                if (json.has("error")) {
                    return "HTTP ${resp.code()}: ${json.getString("error")}"
                }
            } catch (e: Exception) {
                if (errorStr.length < 100) return "HTTP ${resp.code()}: $errorStr"
            }
        }
        val defaultMsg = resp.message().ifBlank { "Error" }
        return "HTTP ${resp.code()}: $defaultMsg"
    }

    private suspend fun fetchVirusTotal(ioc: String, type: IocType): SourceResult {
        return try {
            when (type) {
                IocType.IPv4, IocType.IPv6 -> {
                    val resp = vtService.getIpReport(ioc)
                    if (!resp.isSuccessful) return SourceResult.Error("virustotal", error = parseErrorBody(resp))
                    val attrs = resp.body()?.data?.attributes
                    val raw = gson.toJson(resp.body())
                    SourceResult.VirusTotal(
                        detectionCount = attrs?.stats?.malicious ?: 0,
                        totalEngines = attrs?.stats?.total ?: 0,
                        categories = attrs?.categories?.values?.distinct() ?: emptyList(),
                        tags = attrs?.tags ?: emptyList(),
                        reputation = attrs?.reputation ?: 0,
                        rawJson = raw
                    )
                }
                IocType.DOMAIN -> {
                    val resp = vtService.getDomainReport(ioc)
                    if (!resp.isSuccessful) return SourceResult.Error("virustotal", error = parseErrorBody(resp))
                    val attrs = resp.body()?.data?.attributes
                    val raw = gson.toJson(resp.body())
                    SourceResult.VirusTotal(
                        detectionCount = attrs?.stats?.malicious ?: 0,
                        totalEngines = attrs?.stats?.total ?: 0,
                        categories = attrs?.categories?.values?.distinct() ?: emptyList(),
                        tags = attrs?.tags ?: emptyList(),
                        reputation = attrs?.reputation ?: 0,
                        rawJson = raw
                    )
                }
                IocType.URL -> {
                    // Try instant GET /urls/{id} using base64 URL ID without padding
                    val urlId = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(ioc.toByteArray())
                    val directResp = vtService.getUrlReport(urlId)
                    if (directResp.isSuccessful && directResp.body() != null) {
                        val attrs = directResp.body()?.data?.attributes
                        val raw = gson.toJson(directResp.body())
                        SourceResult.VirusTotal(
                            detectionCount = attrs?.stats?.malicious ?: 0,
                            totalEngines = attrs?.stats?.total ?: 0,
                            categories = attrs?.categories?.values?.distinct() ?: emptyList(),
                            tags = attrs?.tags ?: emptyList(),
                            reputation = attrs?.reputation ?: 0,
                            rawJson = raw
                        )
                    } else {
                        // Fallback to submitting new analysis
                        val submitResp = vtService.submitUrl(ioc)
                        if (!submitResp.isSuccessful) return SourceResult.Error("virustotal", error = parseErrorBody(submitResp))
                        val analysisId = submitResp.body()?.data?.id ?: return SourceResult.Error("virustotal", error = "No analysis ID")
                        kotlinx.coroutines.delay(3000)
                        val resp = vtService.getUrlAnalysis(analysisId)
                        if (!resp.isSuccessful) return SourceResult.Error("virustotal", error = parseErrorBody(resp))
                        val attrs = resp.body()?.data?.attributes
                        val raw = gson.toJson(resp.body())
                        SourceResult.VirusTotal(
                            detectionCount = attrs?.stats?.malicious ?: 0,
                            totalEngines = attrs?.stats?.total ?: 0,
                            rawJson = raw
                        )
                    }
                }
                IocType.MD5, IocType.SHA1, IocType.SHA256 -> {
                    val resp = vtService.getFileReport(ioc)
                    if (!resp.isSuccessful) return SourceResult.Error("virustotal", error = parseErrorBody(resp))
                    val attrs = resp.body()?.data?.attributes
                    val raw = gson.toJson(resp.body())
                    SourceResult.VirusTotal(
                        detectionCount = attrs?.stats?.malicious ?: 0,
                        totalEngines = attrs?.stats?.total ?: 0,
                        tags = attrs?.tags ?: emptyList(),
                        reputation = attrs?.reputation ?: 0,
                        rawJson = raw
                    )
                }
                else -> SourceResult.Error("virustotal", error = "Unsupported IOC type")
            }
        } catch (e: Exception) {
            SourceResult.Error("virustotal", error = e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchAbuseIPDB(ip: String): SourceResult {
        return try {
            val resp = abuseService.checkIp(ip)
            if (!resp.isSuccessful) return SourceResult.Error("abuseipdb", error = parseErrorBody(resp))
            val data = resp.body()?.data
            val raw = gson.toJson(resp.body())
            SourceResult.AbuseIPDB(
                abuseConfidenceScore = data?.abuseConfidenceScore ?: 0,
                totalReports = data?.totalReports ?: 0,
                numDistinctUsers = data?.numDistinctUsers ?: 0,
                lastReportedAt = data?.lastReportedAt,
                countryCode = data?.countryCode,
                isp = data?.isp,
                usageType = data?.usageType,
                isTor = data?.isTor ?: false,
                rawJson = raw
            )
        } catch (e: Exception) {
            SourceResult.Error("abuseipdb", error = e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchShodan(ioc: String, type: IocType): SourceResult {
        return try {
            val ip = if (type == IocType.DOMAIN) {
                val resolved = shodanService.resolveDns(ioc)
                if (!resolved.isSuccessful) return SourceResult.Error("shodan", error = parseErrorBody(resolved))
                resolved.body()?.get(ioc) ?: return SourceResult.Error("shodan", error = "DNS resolution returned empty")
            } else ioc

            val resp = if (prefs.shodanApiKey.isNotBlank()) {
                shodanService.getHostInfo(ip)
            } else null

            if (resp != null && resp.isSuccessful && resp.body() != null) {
                val body = resp.body()
                val raw = gson.toJson(body)
                return SourceResult.Shodan(
                    ports = body?.ports ?: emptyList(),
                    services = body?.data?.mapNotNull { pd ->
                        pd.port?.let { port ->
                            ShodanService(
                                port = port,
                                transport = pd.transport ?: "tcp",
                                product = pd.product,
                                version = pd.version
                            )
                        }
                    } ?: emptyList(),
                    country = body?.countryName,
                    org = body?.org,
                    isp = body?.isp,
                    cves = body?.vulns?.keys?.toList() ?: emptyList(),
                    hostnames = body?.hostnames ?: emptyList(),
                    os = body?.os,
                    rawJson = raw
                )
            }

            // Fallback: Query Shodan InternetDB (100% Free, no API key required)
            val idbResp = shodanService.getInternetDbInfo(ip)
            if (idbResp.isSuccessful && idbResp.body() != null) {
                val body = idbResp.body()
                val raw = gson.toJson(body)
                return SourceResult.Shodan(
                    ports = body?.ports ?: emptyList(),
                    cves = body?.vulns ?: emptyList(),
                    hostnames = body?.hostnames ?: emptyList(),
                    rawJson = raw
                )
            }

            if (resp != null && !resp.isSuccessful) {
                return SourceResult.Error("shodan", error = parseErrorBody(resp))
            }

            return SourceResult.Error("shodan", error = "Shodan data unavailable")
        } catch (e: Exception) {
            SourceResult.Error("shodan", error = e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchOtx(ioc: String, type: IocType): SourceResult {
        return try {
            val resp = when (type) {
                IocType.IPv4 -> otxService.getIpGeneral(ioc)
                IocType.IPv6 -> otxService.getIpv6General(ioc)
                IocType.DOMAIN -> otxService.getDomainGeneral(ioc)
                IocType.URL -> otxService.getUrlGeneral(ioc)
                IocType.MD5, IocType.SHA1, IocType.SHA256 -> otxService.getFileGeneral(ioc)
                else -> return SourceResult.Error("otx", error = "Unsupported IOC type")
            }
            if (!resp.isSuccessful) return SourceResult.Error("otx", error = parseErrorBody(resp))
            val body = resp.body()
            val raw = gson.toJson(body)
            val pulses = body?.pulseInfo?.pulses ?: emptyList()
            SourceResult.OTX(
                pulseCount = body?.pulseInfo?.count ?: 0,
                tags = pulses.flatMap { it.tags ?: emptyList() }.distinct(),
                malwareFamilies = pulses.flatMap {
                    it.malwareFamilies?.mapNotNull { f -> f.id } ?: emptyList()
                }.distinct(),
                adversaries = pulses.mapNotNull { it.adversary }.distinct(),
                industries = pulses.flatMap { it.industries ?: emptyList() }.distinct(),
                rawJson = raw
            )
        } catch (e: Exception) {
            SourceResult.Error("otx", error = e.message ?: "Unknown error")
        }
    }

    private suspend fun fetchAbuseCh(ioc: String, type: IocType): SourceResult {
        return try {
            when (type) {
                IocType.URL -> {
                    val resp = abuseChService.checkUrl(url = ioc)
                    if (resp.isSuccessful && resp.body() != null) {
                        val body = resp.body()!!
                        val raw = gson.toJson(body)
                        val isOnline = body.urlStatus.equals("online", ignoreCase = true)
                        val isMalicious = body.queryStatus.equals("ok", ignoreCase = true)
                        SourceResult.AbuseCh(
                            isFlagged = isMalicious,
                            status = body.urlStatus ?: body.queryStatus,
                            threatType = body.threat,
                            reporter = body.reporter,
                            tags = body.tags ?: emptyList(),
                            rawJson = raw
                        )
                    } else {
                        SourceResult.AbuseCh(isFlagged = false, status = "clean", rawJson = null)
                    }
                }
                IocType.DOMAIN, IocType.IPv4, IocType.IPv6 -> {
                    val tfResp = abuseChService.searchIoc(searchTerm = ioc)
                    if (tfResp.isSuccessful && tfResp.body() != null) {
                        val body = tfResp.body()!!
                        val raw = gson.toJson(body)
                        val firstMatch = body.data?.firstOrNull()
                        val isFlagged = body.queryStatus.equals("ok", ignoreCase = true) && firstMatch != null
                        SourceResult.AbuseCh(
                            isFlagged = isFlagged,
                            status = if (isFlagged) "flagged" else "clean",
                            threatType = firstMatch?.threatType,
                            signature = firstMatch?.malwarePrintable,
                            reporter = firstMatch?.reporter,
                            tags = firstMatch?.tags ?: emptyList(),
                            confidenceLevel = firstMatch?.confidenceLevel ?: 0,
                            rawJson = raw
                        )
                    } else {
                        SourceResult.AbuseCh(isFlagged = false, status = "clean", rawJson = null)
                    }
                }
                IocType.MD5, IocType.SHA1, IocType.SHA256 -> {
                    val mbResp = abuseChService.checkHash(hash = ioc)
                    if (mbResp.isSuccessful && mbResp.body() != null) {
                        val body = mbResp.body()!!
                        val raw = gson.toJson(body)
                        val firstMatch = body.data?.firstOrNull()
                        val isFlagged = body.queryStatus.equals("ok", ignoreCase = true) && firstMatch != null
                        SourceResult.AbuseCh(
                            isFlagged = isFlagged,
                            status = if (isFlagged) "flagged" else "clean",
                            signature = firstMatch?.signature,
                            threatType = firstMatch?.fileType,
                            reporter = firstMatch?.reporter,
                            tags = firstMatch?.tags ?: emptyList(),
                            rawJson = raw
                        )
                    } else {
                        SourceResult.AbuseCh(isFlagged = false, status = "clean", rawJson = null)
                    }
                }
                IocType.UNKNOWN -> SourceResult.Error("abusech", error = "Unknown IOC type")
            }
        } catch (e: Exception) {
            SourceResult.Error("abusech", error = e.message ?: "Unknown error")
        }
    }

    // ─── Mapper ─────────────────────────────────────────────────────────────

    private val sourceResultType = object : TypeToken<Map<String, Any>>() {}.type

    private fun LookupEntity.toDomain(): LookupResult = LookupResult(
        id = id,
        ioc = ioc,
        iocType = IocType.valueOf(iocType),
        verdict = Verdict.valueOf(verdict),
        timestamp = timestamp,
        sources = deserializeSources(sourcesJson),
        isBookmarked = isBookmarked,
        bookmarkNote = bookmarkNote
    )

    private fun LookupResult.toEntity(): LookupEntity = LookupEntity(
        id = id,
        ioc = ioc,
        iocType = iocType.name,
        verdict = verdict.name,
        sourcesJson = serializeSources(sources),
        timestamp = timestamp,
        isBookmarked = isBookmarked,
        bookmarkNote = bookmarkNote
    )

    private fun serializeSources(sources: Map<String, SourceResult>): String {
        val serializable = sources.mapValues { (_, v) -> sourceResultToMap(v) }
        return gson.toJson(serializable)
    }

    private fun sourceResultToMap(result: SourceResult): Map<String, Any?> {
        return when (result) {
            is SourceResult.VirusTotal -> mapOf(
                "_type" to "virustotal",
                "detectionCount" to result.detectionCount,
                "totalEngines" to result.totalEngines,
                "categories" to result.categories,
                "tags" to result.tags,
                "reputation" to result.reputation,
                "rawJson" to result.rawJson
            )
            is SourceResult.AbuseIPDB -> mapOf(
                "_type" to "abuseipdb",
                "abuseConfidenceScore" to result.abuseConfidenceScore,
                "totalReports" to result.totalReports,
                "numDistinctUsers" to result.numDistinctUsers,
                "lastReportedAt" to result.lastReportedAt,
                "countryCode" to result.countryCode,
                "isp" to result.isp,
                "usageType" to result.usageType,
                "isTor" to result.isTor,
                "rawJson" to result.rawJson
            )
            is SourceResult.Shodan -> mapOf(
                "_type" to "shodan",
                "ports" to result.ports,
                "country" to result.country,
                "org" to result.org,
                "isp" to result.isp,
                "cves" to result.cves,
                "hostnames" to result.hostnames,
                "os" to result.os,
                "rawJson" to result.rawJson
            )
            is SourceResult.OTX -> mapOf(
                "_type" to "otx",
                "pulseCount" to result.pulseCount,
                "tags" to result.tags,
                "malwareFamilies" to result.malwareFamilies,
                "adversaries" to result.adversaries,
                "industries" to result.industries,
                "rawJson" to result.rawJson
            )
            is SourceResult.AbuseCh -> mapOf(
                "_type" to "abusech",
                "isFlagged" to result.isFlagged,
                "status" to result.status,
                "threatType" to result.threatType,
                "signature" to result.signature,
                "reporter" to result.reporter,
                "tags" to result.tags,
                "confidenceLevel" to result.confidenceLevel,
                "rawJson" to result.rawJson
            )
            is SourceResult.CustomFeed -> mapOf(
                "_type" to "customfeed",
                "feedName" to result.feedName,
                "isFlagged" to result.isFlagged,
                "summary" to result.summary,
                "responseCode" to result.responseCode,
                "rawJson" to result.rawJson
            )
            is SourceResult.Error -> mapOf(
                "_type" to "error",
                "sourceName" to result.sourceName,
                "error" to result.error
            )
            is SourceResult.Loading -> mapOf("_type" to "loading")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun deserializeSources(json: String): Map<String, SourceResult> {
        return try {
            val raw = gson.fromJson<Map<String, Map<String, Any?>>>(json, sourceResultType)
            raw.mapValues { (key, v) -> mapToSourceResult(key, v) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToSourceResult(sourceKey: String, map: Map<String, Any?>): SourceResult {
        return when (map["_type"]) {
            "virustotal" -> SourceResult.VirusTotal(
                detectionCount = (map["detectionCount"] as? Double)?.toInt() ?: 0,
                totalEngines = (map["totalEngines"] as? Double)?.toInt() ?: 0,
                categories = (map["categories"] as? List<String>) ?: emptyList(),
                tags = (map["tags"] as? List<String>) ?: emptyList(),
                reputation = (map["reputation"] as? Double)?.toInt() ?: 0,
                rawJson = map["rawJson"] as? String
            )
            "abuseipdb" -> SourceResult.AbuseIPDB(
                abuseConfidenceScore = (map["abuseConfidenceScore"] as? Double)?.toInt() ?: 0,
                totalReports = (map["totalReports"] as? Double)?.toInt() ?: 0,
                numDistinctUsers = (map["numDistinctUsers"] as? Double)?.toInt() ?: 0,
                lastReportedAt = map["lastReportedAt"] as? String,
                countryCode = map["countryCode"] as? String,
                isp = map["isp"] as? String,
                usageType = map["usageType"] as? String,
                isTor = map["isTor"] as? Boolean ?: false,
                rawJson = map["rawJson"] as? String
            )
            "shodan" -> SourceResult.Shodan(
                ports = (map["ports"] as? List<Double>)?.map { it.toInt() } ?: emptyList(),
                country = map["country"] as? String,
                org = map["org"] as? String,
                isp = map["isp"] as? String,
                cves = (map["cves"] as? List<String>) ?: emptyList(),
                hostnames = (map["hostnames"] as? List<String>) ?: emptyList(),
                os = map["os"] as? String,
                rawJson = map["rawJson"] as? String
            )
            "otx" -> SourceResult.OTX(
                pulseCount = (map["pulseCount"] as? Double)?.toInt() ?: 0,
                tags = (map["tags"] as? List<String>) ?: emptyList(),
                malwareFamilies = (map["malwareFamilies"] as? List<String>) ?: emptyList(),
                adversaries = (map["adversaries"] as? List<String>) ?: emptyList(),
                industries = (map["industries"] as? List<String>) ?: emptyList(),
                rawJson = map["rawJson"] as? String
            )
            "abusech" -> SourceResult.AbuseCh(
                isFlagged = map["isFlagged"] as? Boolean ?: false,
                status = map["status"] as? String,
                threatType = map["threatType"] as? String,
                signature = map["signature"] as? String,
                reporter = map["reporter"] as? String,
                tags = (map["tags"] as? List<String>) ?: emptyList(),
                confidenceLevel = (map["confidenceLevel"] as? Double)?.toInt() ?: 0,
                rawJson = map["rawJson"] as? String
            )
            "customfeed" -> SourceResult.CustomFeed(
                feedName = (map["feedName"] as? String) ?: sourceKey,
                isFlagged = map["isFlagged"] as? Boolean ?: false,
                summary = map["summary"] as? String,
                responseCode = (map["responseCode"] as? Double)?.toInt() ?: 200,
                rawJson = map["rawJson"] as? String
            )
            "error" -> SourceResult.Error(
                sourceName = (map["sourceName"] as? String) ?: sourceKey,
                error = map["error"] as? String ?: "Error"
            )
            else -> SourceResult.Error(sourceName = sourceKey, error = "Unknown type")
        }
    }
}
