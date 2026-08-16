package com.example.ioclookup.di

import com.example.ioclookup.data.remote.abuseipdb.AbuseIPDBService
import com.example.ioclookup.data.remote.otx.OtxService
import com.example.ioclookup.data.remote.shodan.ShodanService
import com.example.ioclookup.data.remote.virustotal.VirusTotalService
import com.example.ioclookup.data.remote.AbuseIPDBKeyInterceptor
import com.example.ioclookup.data.remote.OtxKeyInterceptor
import com.example.ioclookup.data.remote.ShodanKeyInterceptor
import com.example.ioclookup.data.remote.VirusTotalKeyInterceptor
import com.example.ioclookup.data.security.SecurePreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    private fun baseOkHttp(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (com.example.ioclookup.BuildConfig.DEBUG) {
            val redactingLogging = okhttp3.Interceptor { chain ->
                val request = chain.request()
                val url = request.url.toString().replace(Regex("([?&]key=)[^&]+"), "$1[REDACTED]")
                android.util.Log.d("IOCLookup_Network", "--> ${request.method} $url")

                val startTime = System.currentTimeMillis()
                val response = chain.proceed(request)
                val duration = System.currentTimeMillis() - startTime

                android.util.Log.d("IOCLookup_Network", "<-- ${response.code} $url (${duration}ms)")
                response
            }
            builder.addInterceptor(redactingLogging)
        }

        return builder
    }

    @Provides
    @Singleton
    fun provideVirusTotalService(prefs: SecurePreferences, gson: Gson): VirusTotalService {
        val client = baseOkHttp()
            .addInterceptor(VirusTotalKeyInterceptor { prefs.vtApiKey })
            .build()
        return Retrofit.Builder()
            .baseUrl("https://www.virustotal.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(VirusTotalService::class.java)
    }

    @Provides
    @Singleton
    fun provideAbuseIPDBService(prefs: SecurePreferences, gson: Gson): AbuseIPDBService {
        val client = baseOkHttp()
            .addInterceptor(AbuseIPDBKeyInterceptor { prefs.abuseApiKey })
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.abuseipdb.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AbuseIPDBService::class.java)
    }

    @Provides
    @Singleton
    fun provideShodanService(prefs: SecurePreferences, gson: Gson): ShodanService {
        val client = baseOkHttp()
            .addInterceptor(ShodanKeyInterceptor { prefs.shodanApiKey })
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.shodan.io/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ShodanService::class.java)
    }

    @Provides
    @Singleton
    fun provideOtxService(prefs: SecurePreferences, gson: Gson): OtxService {
        val client = baseOkHttp()
            .addInterceptor(OtxKeyInterceptor { prefs.otxApiKey })
            .build()
        return Retrofit.Builder()
            .baseUrl("https://otx.alienvault.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OtxService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = baseOkHttp().build()

    @Provides
    @Singleton
    fun provideAbuseChService(gson: Gson): com.example.ioclookup.data.remote.abusech.AbuseChService {
        val client = baseOkHttp().build()
        return Retrofit.Builder()
            .baseUrl("https://urlhaus-api.abuse.ch/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(com.example.ioclookup.data.remote.abusech.AbuseChService::class.java)
    }
}
