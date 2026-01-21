package com.example.elapormayonglor.ui.report

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.elapormayonglor.R
import com.example.elapormayonglor.data.model.Report
import com.example.elapormayonglor.data.repository.ReportRepository
import com.example.elapormayonglor.databinding.ActivityDetailReportBinding
import com.example.elapormayonglor.utils.DateUtils
import com.example.elapormayonglor.viewmodel.ReportViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.example.elapormayonglor.ui.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DetailReportActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailReportBinding
    private lateinit var viewModel: ReportViewModel
    private var initialReport: Report? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()

        // Ambil data dari Intent
        initialReport = intent.getParcelableExtra("EXTRA_REPORT")

        if (initialReport != null) {
            updateUI(initialReport!!)
            setupMap(initialReport!!.latitude, initialReport!!.longitude)
            viewModel.loadDetailReport(initialReport!!.reportId)
        } else {
            Toast.makeText(this, "Data error", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupObservers()
        setupListeners()
    }

    private fun setupViewModel() {
        val repository = ReportRepository()
        val factory = ViewModelFactory(reportRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.detailReport.observe(this) { updatedReport ->
            updateUI(updatedReport)
        }
        viewModel.operationResult.observe(this) { result ->
            result.onFailure { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.fabSupport.setOnClickListener {
            initialReport?.reportId?.let { id -> viewModel.toggleSupport(id) }
        }

        // --- FITUR BUKA PETA ---
        binding.btnOpenMap.setOnClickListener {
            val lat = initialReport?.latitude ?: 0.0
            val lng = initialReport?.longitude ?: 0.0
            val label = Uri.encode(initialReport?.judul ?: "Lokasi Laporan")

            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")

            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

    private fun updateUI(data: Report) {
        binding.apply {
            tvDetailTitle.text = data.judul
            tvDetailDesc.text = data.deskripsi

            // Format Tanggal Cantik
            tvDetailDate.text = DateUtils.formatToDisplay(data.createdAt, isDetail = true)

            chipCategory.text = data.kategori
            tvAddress.text = data.alamatLokasi

            tvStatusBadge.text = data.status.uppercase()
            tvStatusBadge.setBackgroundColor(getStatusColor(data.status))

            // Foto dari Pelapor
            Glide.with(this@DetailReportActivity)
                .load(data.fotoUrl)
                .placeholder(R.drawable.bg_header_gradient)
                .into(ivDetailImage)

            // ============================================================
            // RESPON ADMIN (FOTO BALASAN & CATATAN)
            // ============================================================
            if (data.catatanAdmin.isNotEmpty() || data.fotoResponAdmin.isNotEmpty()) {
                cvAdminResponse.visibility = View.VISIBLE
                tvAdminNote.text = data.catatanAdmin.ifEmpty { "Laporan telah ditangani oleh petugas." }

                // Cek Foto Bukti dari Admin
                if (data.fotoResponAdmin.isNotEmpty()) {
                    ivAdminProof.visibility = View.VISIBLE
                    Glide.with(this@DetailReportActivity)
                        .load(data.fotoResponAdmin)
                        .placeholder(R.color.gray_background)
                        .into(ivAdminProof)
                } else {
                    ivAdminProof.visibility = View.GONE
                }
            } else {
                cvAdminResponse.visibility = View.GONE
            }

            val uid = viewModel.getCurrentUid()
            val isSupported = if (uid != null) data.pendukung.contains(uid) else false
            updateSupportButton(isSupported, data.pendukung.size)
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "Diproses" -> Color.parseColor("#FF9800") // Oranye
            "Selesai" -> Color.parseColor("#4CAF50")  // Hijau
            "Ditolak" -> Color.parseColor("#F44336")  // Merah
            else -> Color.parseColor("#9E9E9E")       // Abu-abu (Terkirim)
        }
    }

    private fun updateSupportButton(isSupported: Boolean, count: Int) {
        binding.fabSupport.text = "Dukung ($count)"
        if (isSupported) {
            // Sudah Didukung (Warna Hijau)
            binding.fabSupport.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#29a22e"))
            binding.fabSupport.setIconResource(R.drawable.ic_success)
        } else {
            // Belum Didukung (Warna Biru Brand)
            binding.fabSupport.backgroundTintList = ContextCompat.getColorStateList(this, R.color.brand_primary)
            binding.fabSupport.setIconResource(R.drawable.ic_dukung) // Pake Thumb Up
        }
    }

    private fun setupMap(lat: Double, lng: Double) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            val loc = LatLng(lat, lng)
            googleMap.addMarker(MarkerOptions().position(loc).title("Lokasi Kejadian"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
            googleMap.uiSettings.isMapToolbarEnabled = false
        }
    }

    override fun onMapReady(p0: GoogleMap) { }
}