package com.example.elapormayonglor.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Report(
    // ID Dokumen otomatis dari Firestore
    @DocumentId
    val reportId: String = "",

    // ID User pelapor (Relasi ke tabel User)
    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("judul")
    @set:PropertyName("judul")
    var judul: String = "",

    @get:PropertyName("deskripsi")
    @set:PropertyName("deskripsi")
    var deskripsi: String = "",

    @get:PropertyName("kategori")
    @set:PropertyName("kategori")
    var kategori: String = "",

    // Foto dari Warga (Pelapor)
    @get:PropertyName("foto_url")
    @set:PropertyName("foto_url")
    var fotoUrl: String = "",

    // Data Lokasi
    @get:PropertyName("latitude")
    @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude")
    @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:PropertyName("alamat_lokasi")
    @set:PropertyName("alamat_lokasi")
    var alamatLokasi: String = "",

    // Status Laporan (Terkirim, Diproses, Selesai, Ditolak)
    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "Terkirim",

    // ============================================================
    // RESPON ADMIN (Sinkron dengan Web Admin)
    // ============================================================

    // Pesan/Tanggapan dari Admin
    @get:PropertyName("catatan_admin")
    @set:PropertyName("catatan_admin")
    var catatanAdmin: String = "",

    // Foto Bukti Pengerjaan dari Admin (Hasil Upload di Web)
    @get:PropertyName("fotoResponAdmin")
    @set:PropertyName("fotoResponAdmin")
    var fotoResponAdmin: String = "",

    // ============================================================
    // FITUR DUKUNGAN (LIKE)
    // ============================================================

    @get:PropertyName("pendukung")
    @set:PropertyName("pendukung")
    var pendukung: List<String> = emptyList(),

    @get:PropertyName("jumlah_dukungan")
    @set:PropertyName("jumlah_dukungan")
    var jumlahDukungan: Int = 0,

    // Tanggal dibuat (Format: dd-MM-yyyy HH:mm)
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: String = ""

) : Parcelable