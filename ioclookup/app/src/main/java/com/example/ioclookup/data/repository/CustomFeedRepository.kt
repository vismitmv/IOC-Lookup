package com.example.ioclookup.data.repository

import com.example.ioclookup.data.local.entity.CustomFeedEntity
import com.example.ioclookup.domain.model.SourceResult
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFeedRepository @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    suspend fun executeCustomFeed(feed: CustomFeedEntity, ioc: String): SourceResult = withContext(Dispatchers.IO) {
        try {
            val formattedUrl = feed.urlTemplate.replace("{ioc}", ioc)
            val requestBuilder = Request.Builder().url(formattedUrl)

            if (!feed.headerName.isNullOrBlank() && !feed.headerValue.isNullOrBlank()) {
                requestBuilder.addHeader(feed.headerName, feed.headerValue)
            }

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val code = response.code
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SourceResult.CustomFeed(
                    feedName = feed.name,
                    isFlagged = false,
                    responseCode = code,
                    rawJson = bodyString,
                    error = "HTTP $code"
                )
            }

            var isFlagged = false
            var summary: String? = null

            if (bodyString.isNotBlank()) {
                try {
                    val json = gson.fromJson(bodyString, JsonObject::class.java)
                    val keyPath = feed.jsonPathMalicious.trim()
                    if (json.has(keyPath)) {
                        val elem = json.get(keyPath)
                        isFlagged = when {
                            elem.isJsonPrimitive && elem.asJsonPrimitive.isBoolean -> elem.asBoolean
                            elem.isJsonPrimitive && elem.asJsonPrimitive.isNumber -> elem.asInt > 0
                            elem.isJsonPrimitive && elem.asJsonPrimitive.isString -> {
                                val s = elem.asString.lowercase()
                                s == "true" || s == "malicious" || s == "online" || s == "bad"
                            }
                            else -> false
                        }
                    } else {
                        // Default search for common fields
                        isFlagged = json.has("malicious") && json.get("malicious").asBoolean
                    }
                    summary = if (isFlagged) "Flagged as Malicious by ${feed.name}" else "Clean / Not Flagged"
                } catch (e: Exception) {
                    summary = "Response received (Code $code)"
                }
            }

            SourceResult.CustomFeed(
                feedName = feed.name,
                isFlagged = isFlagged,
                summary = summary,
                responseCode = code,
                rawJson = bodyString
            )
        } catch (e: Exception) {
            SourceResult.CustomFeed(
                feedName = feed.name,
                isFlagged = false,
                responseCode = 0,
                error = e.message ?: "Connection failed"
            )
        }
    }
}
