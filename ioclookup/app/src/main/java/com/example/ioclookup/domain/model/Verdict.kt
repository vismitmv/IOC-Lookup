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
            vtRatio: Double? = null,
            abuseScore: Int? = null,
            otxPulses: Int? = null
        ): Verdict {
            var weightedScore = 0.0
            var totalWeight = 0.0

            vtRatio?.let {
                weightedScore += it * 0.40
                totalWeight += 0.40
            }
            abuseScore?.let {
                weightedScore += (it / 100.0) * 0.35
                totalWeight += 0.35
            }
            otxPulses?.let {
                val normalized = (it.coerceAtMost(50) / 50.0)
                weightedScore += normalized * 0.25
                totalWeight += 0.25
            }

            if (totalWeight == 0.0) return UNKNOWN

            val score = weightedScore / totalWeight

            return when {
                score >= 0.60 -> MALICIOUS
                score >= 0.15 -> SUSPICIOUS
                else -> CLEAN
            }
        }
    }
}
