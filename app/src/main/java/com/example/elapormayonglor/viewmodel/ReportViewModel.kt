package com.example.elapormayonglor.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elapormayonglor.data.model.Report
import com.example.elapormayonglor.data.repository.ReportRepository
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(private val repository: ReportRepository) : ViewModel() {

    // --- STATE UI ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationResult = MutableLiveData<Result<String>>()
    val operationResult: LiveData<Result<String>> = _operationResult

    // --- DATA LIST ---
    private val _allReports = MutableLiveData<List<Report>>()
    private val _filteredReports = MutableLiveData<List<Report>>()
    val homeReports: LiveData<List<Report>> = _filteredReports

    private val _myReportsMaster = MutableLiveData<List<Report>>()
    private val _myReportsFiltered = MutableLiveData<List<Report>>()
    val historyReports: LiveData<List<Report>> = _myReportsFiltered

    private val _supportedReports = MutableLiveData<List<Report>>()
    val supportedReports: LiveData<List<Report>> = _supportedReports

    private val _detailReport = MutableLiveData<Report>()
    val detailReport: LiveData<Report> = _detailReport

    private var snapshotListener: ListenerRegistration? = null

    // =========================================================================
    //  CREATE & UPDATE REPORT
    // =========================================================================
    fun createReport(uri: Uri, title: String, desc: String, category: String, lat: Double, lng: Double, address: String) {
        _isLoading.value = true
        repository.uploadImage(uri)
            .addOnSuccessListener { downloadUri ->
                saveReportToFirestore(downloadUri.toString(), title, desc, category, lat, lng, address)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _operationResult.value = Result.failure(Exception("Gagal upload gambar: ${e.message}"))
            }
    }

    private fun saveReportToFirestore(photoUrl: String, title: String, desc: String, category: String, lat: Double, lng: Double, address: String) {
        val uid = repository.getCurrentUserUid()
        if (uid == null) {
            _isLoading.value = false
            _operationResult.value = Result.failure(Exception("User invalid"))
            return
        }
        val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val report = Report(userId = uid, judul = title, deskripsi = desc, kategori = category, fotoUrl = photoUrl, latitude = lat, longitude = lng, alamatLokasi = address, createdAt = currentDate)

        repository.createReport(report)
            .addOnSuccessListener {
                _isLoading.value = false
                _operationResult.value = Result.success("Laporan terkirim!")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _operationResult.value = Result.failure(e)
            }
    }

    // Fungsi untuk Update (Fitur Edit)
    fun updateReport(reportId: String, title: String, desc: String, category: String, address: String) {
        _isLoading.value = true
        val updatedData = mapOf(
            "judul" to title,
            "deskripsi" to desc,
            "kategori" to category,
            "alamat_lokasi" to address
        )

        repository.updateReport(reportId, updatedData)
            .addOnSuccessListener {
                _isLoading.value = false
                _operationResult.value = Result.success("Laporan berhasil diperbarui!")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _operationResult.value = Result.failure(e)
            }
    }

    // =========================================================================
    //  DELETE REPORT
    // =========================================================================
    fun deleteReport(reportId: String, imageUrl: String?) {
        _isLoading.value = true
        repository.deleteReport(reportId, imageUrl)
            .addOnSuccessListener {
                _isLoading.value = false
                _operationResult.value = Result.success("Laporan berhasil dihapus!")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _operationResult.value = Result.failure(e)
            }
    }

    // =========================================================================
    //  LOAD DATA (REALTIME SNAPSHOTS)
    // =========================================================================

    fun loadAllReports() {
        _isLoading.value = true
        snapshotListener?.remove()
        snapshotListener = repository.getAllReportsQuery().addSnapshotListener { snapshot, e ->
            _isLoading.value = false
            if (e != null) { _operationResult.value = Result.failure(e); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.toObjects(Report::class.java)
                _allReports.value = list
                _filteredReports.value = list
            }
        }
    }

    fun loadMyReports() {
        val uid = repository.getCurrentUserUid() ?: return
        _isLoading.value = true
        repository.getUserReportsQuery(uid).addSnapshotListener { snapshot, e ->
            _isLoading.value = false
            if (e != null) { _operationResult.value = Result.failure(e); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.toObjects(Report::class.java)
                _myReportsMaster.value = list
                _myReportsFiltered.value = list
            }
        }
    }

    fun loadSupportedReports() {
        val uid = repository.getCurrentUserUid() ?: return
        _isLoading.value = true
        repository.getSupportedReportsQuery(uid).addSnapshotListener { snapshot, e ->
            _isLoading.value = false
            if (e != null) { _operationResult.value = Result.failure(e); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.toObjects(Report::class.java)
                _supportedReports.value = list.sortedByDescending { it.createdAt }
            }
        }
    }

    // =========================================================================
    //  FILTER & INTERAKSI
    // =========================================================================

    fun filterHomeByCategory(category: String) {
        val master = _allReports.value ?: emptyList()
        _filteredReports.value = if (category == "Semua" || category == "All") master else master.filter { it.kategori == category }
    }

    fun filterHistoryByStatus(status: String) {
        val master = _myReportsMaster.value ?: emptyList()
        _myReportsFiltered.value = if (status == "Semua" || status == "All") master else master.filter { it.status.equals(status, ignoreCase = true) }
    }

    fun loadDetailReport(reportId: String) {
        repository.getReportDetailRef(reportId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val report = snapshot.toObject(Report::class.java)
                report?.let { _detailReport.value = it }
            }
        }
    }

    fun toggleSupport(reportId: String) {
        val uid = repository.getCurrentUserUid() ?: return
        repository.toggleSupport(reportId, uid)
    }

    fun getCurrentUid() = repository.getCurrentUserUid()

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}