package com.example.ioclookup.domain.model

/**
 * Overall verdict for an IOC lookup, derived from weighted scoring across all sources.
 */
enum class Verdict(val displayName: String, val color: Long) {
    CLEAN("Clean", 0xFF2ECC71),
    SUSPICIOUS("Suspicious", 0xFFF39C12),
    MALICIOUS("Malicious", 0xFFE74C3C),
    UNKNOWN("Unknown", 0xFF95A5A6);

    companion object {
        /**
         * Derives a verdict from weighted source signals.
         *
         * @param vtRatio      VirusTotal detection ratio [0.0, 1.0] — weight 40%
         * @param abuseScore   AbuseIPDB confidence score [0, 100] — weight 35%
         * @param otxPulses    AlienVault OTX pulse count — weight 25% (capped influence)
         */
        fun fromScores(
            vtCount: Int? = null,
            vtRatio: Double? = null,
            abuseScore: Int? = null,
            otxPulses: Int? = null,
            abuseChFlagged: Boolean? = null,
            customFeedsFlagged: Int? = null
        ): Verdict {
            // Instant override for high-confidence malicious signals
            val isVtMalicious = (vtCount ?: 0) >= 3 || (vtRatio ?: 0.0) >= 0.05
            val isAbuseMalicious = (abuseScore ?: 0) >= 50
            val isOtxMalicious = (otxPulses ?: 0) >= 3
            val isAbuseChMalicious = abuseChFlagged == true
            val isCustomMalicious = (customFeedsFlagged ?: 0) > 0

            var maliciousSignalCount = 0
            if (isVtMalicious) maliciousSignalCount++
            if (isAbuseMalicious) maliciousSignalCount++
            if (isOtxMalicious) maliciousSignalCount++
            if (isAbuseChMalicious) maliciousSignalCount++
            if (isCustomMalicious) maliciousSignalCount++

            if (maliciousSignalCount >= 1 || (vtCount ?: 0) >= 2) {
                return MALICIOUS
            }

            // Suspicious signals
            val isVtSuspicious = (vtCount ?: 0) >= 1
            val isAbuseSuspicious = (abuseScore ?: 0) >= 15
            val isOtxSuspicious = (otxPulses ?: 0) >= 1

            if (isVtSuspicious || isAbuseSuspicious || isOtxSuspicious) {
                return SUSPICIOUS
            }

            // Check if any sources actually completed
            val hasData = vtRatio != null || abuseScore != null || otxPulses != null || abuseChFlagged != null || customFeedsFlagged != null
            return if (hasData) CLEAN else UNKNOWN
        }
    }
}
