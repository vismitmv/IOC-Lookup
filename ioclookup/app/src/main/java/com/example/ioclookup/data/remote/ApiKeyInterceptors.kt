package com.example.ioclookup.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Generic API key interceptor. Each Retrofit instance gets its own
 * instance configured with the appropriate header name and key value.
 */
class ApiKeyInterceptor(
    private val headerName: String,
    private val keyProvider: () -> String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = keyProvider()
        val request = if (key.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader(headerName, key)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

/** AbuseIPDB uses a "Key" header. */
class AbuseIPDBKeyInterceptor(keyProvider: () -> String) : Interceptor {
    private val inner = ApiKeyInterceptor("Key", keyProvider)
    override fun intercept(chain: Interceptor.Chain): Response = inner.intercept(chain)
}

/** OTX uses "X-OTX-API-KEY" header. */
class OtxKeyInterceptor(keyProvider: () -> String) : Interceptor {
    private val inner = ApiKeyInterceptor("X-OTX-API-KEY", keyProvider)
    override fun intercept(chain: Interceptor.Chain): Response = inner.intercept(chain)
}

/** VirusTotal uses "x-apikey" header. */
class VirusTotalKeyInterceptor(keyProvider: () -> String) : Interceptor {
    private val inner = ApiKeyInterceptor("x-apikey", keyProvider)
    override fun intercept(chain: Interceptor.Chain): Response = inner.intercept(chain)
}

/** Shodan uses "key" query parameter, but we inject via header wrapper. */
class ShodanKeyInterceptor(private val keyProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = keyProvider()
        val request = if (key.isNotBlank()) {
            val newUrl = chain.request().url.newBuilder()
                .addQueryParameter("key", key)
                .build()
            chain.request().newBuilder().url(newUrl).build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
