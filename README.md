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

## 📋 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
