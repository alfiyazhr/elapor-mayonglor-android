package com.example.elapormayonglor.ui.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.elapormayonglor.R
import com.example.elapormayonglor.data.model.Report
import com.example.elapormayonglor.data.repository.ReportRepository
import com.example.elapormayonglor.databinding.ActivityCreateReportBinding
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.ReportViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.example.elapormayonglor.ui.BaseActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class CreateReportActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateReportBinding
    private lateinit var viewModel: ReportViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var imageUri: Uri? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var isToolbarShown = false
    private var isManualAddress = false

    // Data untuk Mode Edit
    private var existingReport: Report? = null
    private var isEditMode = false

    private val categories = listOf(
        "Infrastruktur",
        "Penerangan Jalan Umum (PJU)",
        "Kebersihan & Lingkungan",
        "Fasilitas Umum (Fasum)",
        "Ketertiban & Keamanan",
        "Lain-lain"
    )

    private val startForProfileImageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                displayImagePreview(uri)
            }
        } else if (result.resultCode == ImagePicker.RESULT_ERROR) {
            ToastUtils.showError(this, ImagePicker.getError(result.data))
        }
    }

    private val startForResultLocation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            currentLatitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            currentLongitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            val pickedAddress = getDetailedAddress(currentLatitude, currentLongitude)

            binding.tvLocation.text = pickedAddress
            binding.etLocation.setText(pickedAddress)
            binding.tilLocation.visibility = View.VISIBLE
            isManualAddress = true
            ToastUtils.showSuccess(this, "Lokasi dipilih dari peta")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) getCurrentLocation()
        else binding.tvLocation.text = "Izin lokasi ditolak"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        checkEditMode() // Cek apakah ini mode Edit atau Baru
        setupClickListeners()
        setupObservers()
        setupLiveValidation()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Hanya ambil lokasi otomatis jika bukan mode Edit
        if (!isEditMode) checkLocationPermission()
    }

    private fun checkEditMode() {
        existingReport = intent.getParcelableExtra("EXTRA_REPORT")
        if (existingReport != null) {
            isEditMode = true
            binding.tvTitleHeader.text = "Edit Laporan"
            binding.tvStickyTitle.text = "Edit Laporan"
            binding.btnSubmit.text = "Update Laporan"

            // Isi data lama ke Form
            existingReport?.let { report ->
                binding.etTitle.setText(report.judul)
                binding.etCategory.setText(report.kategori, false)
                binding.etDescription.setText(report.deskripsi)
                binding.etLocation.setText(report.alamatLokasi)
                binding.tvLocation.text = report.alamatLokasi
                currentLatitude = report.latitude
                currentLongitude = report.longitude

                // Tampilkan gambar lama dari URL
                if (report.fotoUrl.isNotEmpty()) {
                    displayImageFromUrl(report.fotoUrl)
                }
            }
        }
    }

    private fun setupViewModel() {
        val repository = ReportRepository()
        val factory = ViewModelFactory(reportRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]
    }

    private fun setupUI() {
        binding.stickyToolbar.translationY = -100f
        binding.stickyToolbar.alpha = 0f
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.etCategory.setAdapter(adapter)
    }

    private fun setupLiveValidation() {
        binding.etTitle.doOnTextChanged { text, _, _, _ ->
            binding.tilTitle.error = if (text.isNullOrBlank()) "Judul wajib diisi" else null
        }
        binding.etDescription.doOnTextChanged { text, _, _, _ ->
            binding.tilDescription.error = if (text.isNullOrBlank()) "Deskripsi wajib diisi" else null
        }
        binding.etLocation.doOnTextChanged { text, _, _, _ ->
            if (isManualAddress && text.isNullOrBlank()) binding.tilLocation.error = "Alamat wajib diisi"
            else binding.tilLocation.error = null
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (binding.etTitle.text.isNullOrBlank()) { binding.tilTitle.error = "Judul wajib"; isValid = false }
        if (binding.etCategory.text.isNullOrBlank()) { binding.tilCategory.error = "Kategori wajib"; isValid = false }
        if (binding.etDescription.text.isNullOrBlank()) { binding.tilDescription.error = "Deskripsi wajib"; isValid = false }

        // Di mode baru wajib foto, di mode edit boleh pakai foto lama (imageUri null gapapa)
        if (!isEditMode && imageUri == null) {
            ToastUtils.showError(this, "Wajib sertakan foto bukti!")
            isValid = false
        }

        if (currentLatitude == 0.0) {
            ToastUtils.showError(this, "Lokasi belum valid")
            isValid = false
        }
        return isValid
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { showLoading(it) }
        viewModel.operationResult.observe(this) { result ->
            result.onSuccess { msg ->
                ToastUtils.showSuccess(this, msg)
                setResult(Activity.RESULT_OK)
                finish()
            }
            result.onFailure { e -> ToastUtils.showError(this, e.message ?: "Gagal") }
        }
    }

    private fun setupClickListeners() {
        binding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView, _, scrollY, _, _ ->
            if (scrollY > 300 && !isToolbarShown) { isToolbarShown = true; animateToolbar(0f, 1f) }
            else if (scrollY <= 300 && isToolbarShown) { isToolbarShown = false; animateToolbar(-100f, 0f) }
        }

        binding.ivBackSticky.setOnClickListener { finish() }

        binding.cvImagePicker.setOnClickListener {
            ImagePicker.with(this).crop(4f, 3f).compress(1024).maxResultSize(1080, 1080)
                .createIntent { intent -> startForProfileImageResult.launch(intent) }
        }

        binding.btnToggleManualLocation.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            startForResultLocation.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            if (validateInput()) {
                val title = binding.etTitle.text.toString().trim()
                val desc = binding.etDescription.text.toString().trim()
                val cat = binding.etCategory.text.toString().trim()
                val addr = if (binding.etLocation.text.isNullOrBlank()) binding.tvLocation.text.toString() else binding.etLocation.text.toString()

                if (isEditMode) {
                    // Update data (tanpa upload foto lagi jika tidak ganti foto)
                    viewModel.updateReport(existingReport!!.reportId, title, desc, cat, addr)
                } else {
                    // Buat laporan baru
                    viewModel.createReport(imageUri!!, title, desc, cat, currentLatitude, currentLongitude, addr)
                }
            }
        }

        binding.ivRefreshLocation.setOnClickListener {
            it.animate().rotation(it.rotation + 360f).setDuration(500).start()
            isManualAddress = false
            binding.tilLocation.visibility = View.GONE
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) getCurrentLocation()
        else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getCurrentLocation() {
        if (isManualAddress) return
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    val addr = getDetailedAddress(currentLatitude, currentLongitude)
                    binding.tvLocation.text = addr
                    binding.etLocation.setText(addr)
                }
            }
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun getDetailedAddress(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale("id", "ID"))
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.get(0)?.getAddressLine(0) ?: "Lokasi: $lat, $lng"
        } catch (e: Exception) { "Lokasi: $lat, $lng" }
    }

    private fun animateToolbar(transY: Float, alpha: Float) {
        binding.stickyToolbar.animate().translationY(transY).alpha(alpha)
            .setDuration(300).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun displayImagePreview(uri: Uri) {
        binding.ivReportImage.apply { setPadding(0, 0, 0, 0); scaleType = ImageView.ScaleType.CENTER_CROP; imageTintList = null }
        binding.tvTapToUpload.visibility = View.GONE
        Glide.with(this).load(uri).into(binding.ivReportImage)
    }

    private fun displayImageFromUrl(url: String) {
        binding.ivReportImage.apply { setPadding(0, 0, 0, 0); scaleType = ImageView.ScaleType.CENTER_CROP; imageTintList = null }
        binding.tvTapToUpload.visibility = View.GONE
        Glide.with(this).load(url).into(binding.ivReportImage)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
        binding.btnSubmit.text = if (isLoading) "Proses..." else if (isEditMode) "Update Laporan" else "Kirim Laporan"
    }
}