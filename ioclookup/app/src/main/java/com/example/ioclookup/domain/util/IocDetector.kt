package com.example.ioclookup.domain.util

import com.example.ioclookup.domain.model.IocType

/**
 * Detects the type of an IOC input string using regex patterns.
 * Priority: Hash → IP → URL → Domain
 */
object IocDetector {

    private val MD5_REGEX = Regex("^[a-fA-F0-9]{32}$")
    private val SHA1_REGEX = Regex("^[a-fA-F0-9]{40}$")
    private val SHA256_REGEX = Regex("^[a-fA-F0-9]{64}$")

    private val IPV4_REGEX = Regex(
        "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )

    // Simplified IPv6: handles full, compressed, and IPv4-mapped forms
    private val IPV6_REGEX = Regex(
        "^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$|" +
                "^::([0-9a-fA-F]{1,4}:){0,5}[0-9a-fA-F]{0,4}$|" +
                "^[0-9a-fA-F]{1,4}::([0-9a-fA-F]{1,4}:){0,4}[0-9a-fA-F]{0,4}$"
    )

    private val URL_REGEX = Regex(
        "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
        RegexOption.IGNORE_CASE
    )

    private val DOMAIN_REGEX = Regex(
        "^(?:[a-zA-Z0-9]" +
                "(?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)" +
                "+[a-zA-Z]{2,}$"
    )

    fun detect(input: String): IocType {
        val trimmed = input.trim()
        return when {
            SHA256_REGEX.matches(trimmed) -> IocType.SHA256
            SHA1_REGEX.matches(trimmed)   -> IocType.SHA1
            MD5_REGEX.matches(trimmed)    -> IocType.MD5
            IPV4_REGEX.matches(trimmed)   -> IocType.IPv4
            IPV6_REGEX.matches(trimmed)   -> IocType.IPv6
            URL_REGEX.matches(trimmed)    -> IocType.URL
            DOMAIN_REGEX.matches(trimmed) -> IocType.DOMAIN
            else                          -> IocType.UNKNOWN
        }
    }

    fun isValid(input: String): Boolean = detect(input) != IocType.UNKNOWN
}
