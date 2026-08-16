# 🛡️ IOC Lookup — Threat Intelligence Investigation App

![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Material 3](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Security](https://img.shields.io/badge/Security-AES256%20Hardware--Encrypted-FF6D00?style=for-the-badge&logo=google-keychain&logoColor=white)

**IOC Lookup** is a native Android security application designed for SOC analysts, incident responders, and threat researchers to investigate Indicators of Compromise (IP addresses, Domains, URLs, and File Hashes) simultaneously across multiple threat intelligence providers.

---

## ✨ Features

- 🔍 **Automated IOC Type Detection:** Auto-detects IPv4, IPv6, Domain, URL, MD5, SHA-1, and SHA-256 inputs on paste or typing.
- ⚡ **Parallel Multi-Source Engines:** Queries multiple threat intelligence providers concurrently using Kotlin Coroutines:
  - **VirusTotal:** Detection ratios, malicious tags, engine breakdowns.
  - **AbuseIPDB:** Abuse confidence scores, report counts, ISP, usage type, country location.
  - **Shodan & Shodan InternetDB:** Open port enumeration, hostnames, running services, CPEs, and CVE vulnerabilities.
  - **AlienVault OTX:** Threat pulse counts, adversary attribution, targeted industries, and malware family tagging.
- ⚖️ **Weighted Verdict Card:** Calculates an overall verdict (`CLEAN`, `SUSPICIOUS`, `MALICIOUS`) based on normalized scores across all providers.
- 📑 **Raw JSON Inspection:** Expandable raw JSON view for every source response with one-tap clipboard copying.
- 📜 **Offline History & Bookmarks:** Encrypted local storage using Room SQLite database with TTL-based caching.
- 📄 **PDF & Text Export:** Generate formatted PDF threat reports or raw markdown/text summaries for sharing via Android intent.
- 🎨 **Modern Cyber Aesthetics:** Dark mode glassmorphism UI built with Jetpack Compose & Material 3.

---

## 🔒 Security Architecture

Built from the ground up for security-conscious professionals:

1. **Hardware-Backed AES-256 Key Storage:** API keys are encrypted using Android Keystore Master Key via `EncryptedSharedPreferences` (`AES256_GCM` & `AES256_SIV`). Credentials never touch plain text `SharedPreferences` or disk.
2. **Zero Logcat Credential Leaks:** Network logging interceptors sanitize query parameters (`key=***`) and redact sensitive API headers (`x-api-key`, `Authorization`, `OTX-API-KEY`). Network logging is disabled entirely in Release builds.
3. **Strict Network Security Config:** Enforces `cleartextTrafficPermitted="false"` across the entire app and restricts connections to TLS 1.2/1.3 with system trust anchors to prevent MITM proxy interception.
4. **Code Obfuscation & Shrinking:** Release builds are minified, resource-shrunk, and obfuscated using **R8 / ProGuard** rules.

---

## 🏗️ Architecture & Stack

- **Architecture:** Clean Architecture + MVVM (UI Layer, Domain Use Cases, Data Repository Layer).
- **Dependency Injection:** Hilt (Dagger 2.60.1).
- **Asynchronous Execution:** Kotlin Coroutines & Flow.
- **Networking:** Retrofit 2 + OkHttp 4 + Gson.
- **Local Persistence:** Room 2.7 + EncryptedSharedPreferences.
- **UI Framework:** Jetpack Compose + Material 3 + Navigation Compose.

---

## 🛠️ Building & Installation

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or JDK 17.
- Android SDK 36 (Min SDK 26 - Android 8.0).

### Build APK
```bash
# Clone repository
git clone https://github.com/vismitmv/ioc-lookup-android.git
cd ioc-lookup-android/ioclookup

# Build Release APK
./gradlew assembleRelease
```
The compiled, obfuscated APK will be generated at:
`app/build/outputs/apk/release/IOC_Lookup_v1.0.0.apk`

---

## 📄 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.
