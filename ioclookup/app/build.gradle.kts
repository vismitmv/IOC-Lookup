plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.vismitmv.ioclookup"
  compileSdk = 36
  defaultConfig {
    applicationId = "com.vismitmv.ioclookup"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    aidl = false
    buildConfig = true
    shaders = false
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "/META-INF/NOTICE.md"
      excludes += "/META-INF/LICENSE.md"
      excludes += "/META-INF/*.kotlin_module"
      excludes += "mozilla/public-suffix-list.txt"
    }
  }

  dependenciesInfo {
      includeInApk = false
      includeInBundle = false
  }
}

base {
  archivesName.set("IOC_Lookup_v1.0.0")
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Lifecycle / ViewModel
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Navigation3
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Hilt
  implementation(libs.hilt.android)
  ksp(libs.hilt.android.compiler)
  implementation(libs.hilt.navigation.compose)
  implementation(libs.hilt.work)
  ksp(libs.hilt.work.compiler)

  // Room
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // Retrofit + OkHttp
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.gson)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.gson)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)

  // Security
  implementation(libs.security.crypto)

  // DataStore
  implementation(libs.datastore.preferences)

  // Coil
  implementation(libs.coil.compose)

  // WorkManager
  implementation(libs.work.runtime.ktx)

  // Tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
}

configurations.all {
    exclude(group = "io.opencensus", module = "opencensus-api")
    exclude(group = "io.opencensus", module = "opencensus-proto")
}
