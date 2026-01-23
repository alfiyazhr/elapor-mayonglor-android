# E-Lapor Mayong Lor (Android Client)

**E-Lapor Mayong Lor** is a location-based mobile application designed to bridge the communication gap between citizens and local government in Mayong Lor Village. This app enables users to report community incidents, track report progress, and access digital administrative services directly from their smartphones.

## Mobile Interface (Screenshots)

| Welcome & Login | Home / Beranda | Report Form |
| :---: | :---: | :---: |
| [img beranda/login] | [img home] | [img form laporan] |

| Tracking Status | History Laporan | Detail Lokasi Map |
| :---: | :---: | :---: |
| [img tracking] | [img history] | [img maps] |

## Key Features
- **Geolocation-based Reporting:** Automatically captures precise incident coordinates using Google Maps Geolocation API.
- **Photo Evidence Integration:** Securely attaches photographic proof of complaints to Firebase Cloud Storage.
- **Real-time Status Tracking:** Users receive instant updates (Pending, Processed, Completed) synced directly from the Web Admin Dashboard.
- **Verified Authentication:** Integrated with Firebase Auth for secure user registration and login.
- **Digital History:** A comprehensive log of past reports for users to monitor village development progress.

## 🛠 Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Firebase Firestore (Real-time synchronization)
- **Storage:** Firebase Cloud Storage
- **Networking:** Retrofit & OkHttp (External API integration)
- **API:** Google Maps SDK & Geolocation API

## System Architecture
This application follows **Clean Architecture** principles to ensure scalability and high maintainability. As part of a serverless ecosystem, this Android client works in tandem with the **[E-Lapor Web Admin Dashboard](MASUKKAN_LINK_REPO_WEB_DISINI)**.

## Security & Configuration
For security reasons, sensitive configuration files (such as `google-services.json`) are excluded from this public repository via `.gitignore` to protect API keys and Firebase credentials.

---
*Developed as a **Final Project (Skripsi)** - Informatics Engineering **Universitas Muria Kudus***.

**Maintainer:** Alfiya Zahrotul Jannah 
