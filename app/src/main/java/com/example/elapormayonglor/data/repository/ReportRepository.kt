package com.example.elapormayonglor.data.repository

import android.net.Uri
import com.example.elapormayonglor.data.model.Report
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ReportRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Ambil UID user aktif untuk relasi data laporan
    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    // --- UPLOAD, SIMPAN & UPDATE ---

    /**
     * Upload foto bukti ke Firebase Storage
     */
    fun uploadImage(imageUri: Uri): Task<Uri> {
        val filename = "IMG_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("laporan/$filename")
        return ref.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) task.exception?.let { throw it }
            ref.downloadUrl
        }
    }

    /**
     * Menyimpan objek Report baru ke koleksi 'reports'
     */
    fun createReport(report: Report): Task<DocumentReference> {
        return db.collection("reports").add(report)
    }

    /**
     * Memperbarui data laporan yang sudah ada (Fitur Edit)
     */
    fun updateReport(reportId: String, updatedData: Map<String, Any>): Task<Void> {
        return db.collection("reports").document(reportId).update(updatedData)
    }

    // --- FITUR DELETE ---

    /**
     * Menghapus laporan dari Firestore dan foto terkait dari Storage (Fitur Delete)
     */
    fun deleteReport(reportId: String, imageUrl: String?): Task<Void> {
        // 1. Hapus dokumen di Firestore
        return db.collection("reports").document(reportId).delete().continueWithTask { task ->
            if (task.isSuccessful && !imageUrl.isNullOrEmpty()) {
                // 2. Jika dokumen berhasil dihapus, hapus juga fotonya di Storage
                val photoRef = storage.getReferenceFromUrl(imageUrl)
                photoRef.delete()
            }
            task
        }
    }

    // --- QUERY DATA ---

    /**
     * Mengambil semua laporan (Home)
     */
    fun getAllReportsQuery(): Query {
        return db.collection("reports")
            .orderBy("created_at", Query.Direction.DESCENDING)
    }

    /**
     * Mengambil laporan milik user sendiri (History)
     */
    fun getUserReportsQuery(uid: String): Query {
        return db.collection("reports")
            .whereEqualTo("userId", uid)
            .orderBy("created_at", Query.Direction.DESCENDING)
    }

    /**
     * Mengambil laporan yang didukung user
     */
    fun getSupportedReportsQuery(uid: String): Query {
        return db.collection("reports")
            .whereArrayContains("pendukung", uid)
    }

    // --- INTERAKSI FITUR DUKUNGAN (TRANSACTIONAL) ---

    fun toggleSupport(reportId: String, uid: String): Task<Void> {
        val docRef = db.collection("reports").document(reportId)
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val report = snapshot.toObject(Report::class.java)
            val currentPendukung = report?.pendukung?.toMutableList() ?: mutableListOf()

            if (currentPendukung.contains(uid)) {
                currentPendukung.remove(uid)
            } else {
                currentPendukung.add(uid)
            }

            transaction.update(docRef, "pendukung", currentPendukung)
            transaction.update(docRef, "jumlah_dukungan", currentPendukung.size)
            null
        }
    }

    fun getReportDetailRef(reportId: String): DocumentReference {
        return db.collection("reports").document(reportId)
    }
}