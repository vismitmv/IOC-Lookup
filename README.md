# 🛡️ IOC Lookup — Mobile Threat Intelligence

**IOC Lookup** is a high-performance Android application built for SOC analysts, incident responders, and security enthusiasts to perform rapid Indicator of Compromise (IOC) lookups across major threat intelligence platforms and offline threat feeds.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg)

---

## ✨ Features

- 🔍 **Multi-Source IOC Scanning**: Query IPs, Domains, URLs, and File Hashes (MD5, SHA-1, SHA-256) simultaneously across:
  - **VirusTotal**
  - **AbuseIPDB**
  - **Shodan**
  - **AlienVault OTX**
- 🛡️ **abuse.ch Suite Integration**: Deep threat intelligence checking against **URLhaus**, **MalwareBazaar**, and **ThreatFox**.
- ⚡ **Offline Custom Threat Feeds**: Import plain-text banlists (e.g., URLhaus Online URLs, BinaryDefense IPs, or custom firewall lists) into an indexed Room database for sub-millisecond local offline matching.
- 🎯 **Automated Verdict Engine**: Real-time verdict calculation with high-confidence threat overrides so flagged threats are never marked as clean.
- 🎨 **Adaptive Theme & Accent Customization**: Full Light/Dark mode support with custom accent color picker swatches & hex code inputs.
- 🔒 **Security & Privacy First**: Direct HTTPS communication with zero telemetry, zero middleman proxy servers, and secure encrypted Android Keystore API storage.

---

## 🛠️ Build & Install

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 36 (minSdk 26)

### Compiling from Source
```bash
git clone https://github.com/vismitmv/IOC-Lookup.git
cd IOC-Lookup/ioclookup
./gradlew assembleRelease
```

---

## 🔑 API Key Setup

IOC Lookup does **not** bundle any API keys. You must provide your own (free-tier) keys:

1. **VirusTotal**: Sign up at [virustotal.com](https://www.virustotal.com/) → API Key in your profile
2. **AbuseIPDB**: Sign up at [abuseipdb.com](https://www.abuseipdb.com/) → API Key in dashboard
3. **Shodan**: Sign up at [shodan.io](https://www.shodan.io/) → API Key in account settings
4. **AlienVault OTX**: Sign up at [otx.alienvault.com](https://otx.alienvault.com/) → API Key in settings

Enter your keys in the app's **Settings** screen. Keys are encrypted locally using Android's EncryptedSharedPreferences (AES-256-GCM) and never leave your device.

**abuse.ch sources** (URLhaus, MalwareBazaar, ThreatFox) do **not** require API keys.

---

## 🔒 Privacy

- **No telemetry or analytics**: IOC Lookup does not collect, transmit, or store any usage data.
- **Direct API communication**: All threat lookups are made directly from your device to the respective API provider (VirusTotal, AbuseIPDB, Shodan, AlienVault OTX, abuse.ch). No intermediary servers are used.
- **Local storage only**: API keys are encrypted with AES-256-GCM via Android Keystore and stored on-device. Search history and bookmarks are stored in a local Room database.
- **No third-party SDKs**: No advertising, tracking, or analytics SDKs are included.

---

## 📋 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
