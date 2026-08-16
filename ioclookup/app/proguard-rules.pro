# ProGuard & R8 Obfuscation & Shrinking Rules for IOC Lookup

# Keep Retrofit models from R8 stripping
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.vismitmv.ioclookup.model.** { *; }
-keep interface com.vismitmv.ioclookup.api.** { *; }
-keep class com.vismitmv.ioclookup.data.remote.** { *; }
-keep class com.vismitmv.ioclookup.domain.model.** { *; }
-keep class com.example.ioclookup.data.remote.** { *; }
-keep class com.example.ioclookup.domain.model.** { *; }

# Keep Gson serialization if used
-keep class com.google.gson.** { *; }
-keepattributes SerializedName

# Keep OkHttp and Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt & Dagger
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Android Crypto / Security
-keep class androidx.security.crypto.** { *; }
