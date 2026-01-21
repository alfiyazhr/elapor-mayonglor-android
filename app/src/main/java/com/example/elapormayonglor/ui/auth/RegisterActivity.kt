package com.example.elapormayonglor.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.elapormayonglor.data.repository.AuthRepository
import com.example.elapormayonglor.databinding.ActivityRegisterBinding
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.AuthViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.example.elapormayonglor.ui.BaseActivity

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupDukuhSpinner()
        setupListeners()
        setupObservers()
        setupLiveValidation()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(this)
        val factory = ViewModelFactory(authRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun setupDukuhSpinner() {
        val listDukuh = arrayOf("Bendowangen", "Krajan", "Gleget", "Karangpanggung")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listDukuh)
        binding.autoCompleteDukuh.setAdapter(adapter)
    }

    private fun setupObservers() {
        // Observer Loading
        viewModel.isLoading.observe(this) { showLoading(it) }

        // Observer Hasil Register (Kunci MVVM: Menangkap pesan spesifik dari ViewModel)
        viewModel.registerResult.observe(this) { result ->
            result.onSuccess { message ->
                // Jika sukses, tampilkan pesan sukses dari ViewModel
                ToastUtils.showSuccess(this, message)
                finish()
            }
            result.onFailure { error ->
                // Jika gagal (NIK duplikat ATAU Email duplikat),
                // tampilkan pesan error spesifik yang sudah dirangkai ViewModel
                ToastUtils.showError(this, error.message ?: "Registrasi Gagal")
            }
        }
    }

    private fun setupListeners() {
        binding.tvLogin.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            if (isFormValid()) {
                val nik = binding.etNik.text.toString().trim()
                val nama = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()

                val rt = binding.etRt.text.toString().trim()
                val rw = binding.etRw.text.toString().trim()
                val dukuh = binding.autoCompleteDukuh.text.toString()

                // Format alamat terstruktur
                val alamatLengkap = "RT $rt / RW $rw, Dukuh $dukuh"

                // Panggil fungsi register di ViewModel
                viewModel.register(nik, nama, email, password, alamatLengkap)
            }
        }
    }

    private fun setupLiveValidation() {
        // NIK 16 Digit
        binding.etNik.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                binding.tilNik.error = "NIK wajib diisi"
            } else if (text.length < 16) {
                binding.tilNik.error = "NIK harus 16 digit"
            } else {
                binding.tilNik.error = null
            }
        }

        // Nama
        binding.etName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) binding.tilName.error = "Nama wajib diisi"
            else binding.tilName.error = null
        }

        // Email
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            val email = text.toString().trim()
            if (email.isEmpty()) {
                binding.tilEmail.error = "Email wajib diisi"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Format email tidak valid"
            } else {
                binding.tilEmail.error = null
            }
        }

        // Password Minimal 8 Karakter
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            if ((text?.length ?: 0) < 8) {
                binding.tilPassword.error = "Password minimal 8 karakter"
            } else {
                binding.tilPassword.error = null
            }
            validateMatchPassword()
        }

        // Konfirmasi Password
        binding.etConfirmPassword.doOnTextChanged { _, _, _, _ ->
            validateMatchPassword()
        }
    }

    private fun validateMatchPassword(): Boolean {
        val pass = binding.etPassword.text.toString()
        val confirmPass = binding.etConfirmPassword.text.toString()
        return if (confirmPass.isNotEmpty() && pass != confirmPass) {
            binding.tilConfirmPassword.error = "Password tidak sama!"
            false
        } else {
            binding.tilConfirmPassword.error = null
            true
        }
    }

    private fun isFormValid(): Boolean {
        val nik = binding.etNik.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        val rt = binding.etRt.text.toString().trim()
        val rw = binding.etRw.text.toString().trim()
        val dukuh = binding.autoCompleteDukuh.text.toString()

        var isValid = true

        if (nik.length < 16) {
            binding.tilNik.error = "NIK harus 16 digit"
            isValid = false
        }
        if (name.isEmpty()) {
            binding.tilName.error = "Nama wajib diisi"
            isValid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Cek kembali email Anda"
            isValid = false
        }
        if (pass.length < 8) {
            binding.tilPassword.error = "Password minimal 8 karakter"
            isValid = false
        }

        // Validasi Alamat menggunakan Toast agar tidak memenuhi UI
        if (rt.isEmpty() || rw.isEmpty() || dukuh.isEmpty()) {
            ToastUtils.showError(this, "Alamat (RT/RW/Dukuh) wajib dilengkapi!")
            isValid = false
        }

        return isValid && validateMatchPassword()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "" else "Daftar Sekarang"
    }
}