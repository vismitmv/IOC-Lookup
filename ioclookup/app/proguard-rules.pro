# ProGuard & R8 Obfuscation & Shrinking Rules for IOC Lookup

# Keep Gson Serialized Data Models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.example.ioclookup.data.remote.** { *; }
-keep class com.example.ioclookup.domain.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers enum * { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Hilt & Dagger
-keep class dagger.hilt.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Android Crypto / Security
-keep class androidx.security.crypto.** { *; }

# SLF4J / Logging optional dependencies
-dontwarn org.slf4j.**

