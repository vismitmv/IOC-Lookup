package com.example.ioclookup.domain.model

/**
 * Represents the type of an Indicator of Compromise (IOC).
 */
enum class IocType(val displayName: String, val icon: String) {
    IPv4("IPv4 Address", "🌐"),
    IPv6("IPv6 Address", "🌐"),
    DOMAIN("Domain / Hostname", "🔗"),
    URL("URL", "🔗"),
    MD5("MD5 Hash", "🔑"),
    SHA1("SHA-1 Hash", "🔑"),
    SHA256("SHA-256 Hash", "🔑"),
    UNKNOWN("Unknown", "❓");

    val isHash: Boolean get() = this == MD5 || this == SHA1 || this == SHA256
    val isIp: Boolean get() = this == IPv4 || this == IPv6
}
