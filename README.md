# E-Lapor Mayong Lor (Android Client)

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)](https://kotlinlang.org/)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange.svg)](https://developer.android.com/topic/architecture)
[![Firebase](https://img.shields.io/badge/Firebase-Latest-yellow.svg)](https://firebase.google.com/)

Aplikasi pelaporan pengaduan masyarakat Desa Mayong Lor berbasis Android Native. Proyek ini dibangun dengan fokus pada kemudahan pelaporan, transparansi status, dan akurasi lokasi menggunakan integrasi Geolocation.

## Antarmuka Aplikasi (Screenshots)

| Login | Beranda | Integrasi Maps |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/d402df64-1fae-4dff-adf5-e6d441a21b31" width="200" /> | <img src="https://github.com/user-attachments/assets/9633b30b-85e2-4634-a131-16ac70d66825" width="200" /> | <img src="https://github.com/user-attachments/assets/1551bff5-09f4-48e2-8f6c-65387367f7f9" width="200" /> |

| Riwayat | Laporan Didukung | Profil & Panduan |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/f6a9a7a8-3e41-42ee-b81e-bccfb1e3da97" width="200" /> | <img src="https://github.com/user-attachments/assets/0ae52ee7-9661-4a16-8de1-45cfe07109b4" width="200" /> | <img src="https://github.com/user-attachments/assets/aa4984c8-b645-4d9b-94a8-475d3bac9113" width="200" /> |

## Fitur Utama
- **Pelaporan Real-time:** Masyarakat dapat mengirim laporan lengkap dengan foto dokumen/kejadian.
- **Integrasi Geolocation:** Penentuan titik lokasi otomatis menggunakan Google Maps SDK untuk akurasi data laporan.
- **Tracking Status:** Memantau progres laporan (Menunggu, Diproses, Selesai) secara transparan.
- **Dukungan Laporan:** Fitur sosial yang memungkinkan sesama warga mendukung laporan yang serupa.
- **Autentikasi Aman:** Sistem login dan registrasi menggunakan Firebase Auth.

## Tech Stack & Arsitektur
Aplikasi ini menerapkan standar pengembangan Android modern:
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI Framework:** Android Jetpack (ViewBinding, LiveData, ViewModel)
- **Backend Service:** Firebase Firestore (Database), Firebase Storage (Images)
- **Maps API:** Google Maps SDK & Fused Location Provider
- **Image Handling:** Glide & ImagePicker

## Keamanan Data
Proyek ini mengikuti *best practice* keamanan dengan tidak melakukan *hardcode* pada API Key. Semua kredensial sensitif dikelola melalui file `local.properties` dan diinjeksi melalui `BuildConfig` untuk mencegah kebocoran data di repositori publik.

---
*Dikembangkan sebagai Tugas Akhir (Skripsi) - Teknik Informatika **Universitas Muria Kudus***.

*Maintainer: **Alfiya Zahrotul Jannah***
