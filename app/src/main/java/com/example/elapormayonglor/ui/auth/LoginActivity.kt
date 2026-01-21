package com.example.elapormayonglor.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.elapormayonglor.data.repository.AuthRepository
import com.example.elapormayonglor.databinding.ActivityLoginBinding
import com.example.elapormayonglor.ui.main.MainActivity
import com.example.elapormayonglor.utils.DialogUtils
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.AuthViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.example.elapormayonglor.ui.BaseActivity

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        checkAutoLogin()
        setupListeners()
        setupObservers()
        setupLiveValidation()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(this)
        val factory = ViewModelFactory(authRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun checkAutoLogin() {
        if (viewModel.checkLoginStatus()) {
            goToDashboard()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        // LOGIKA EXPERT: Menampilkan pesan error spesifik dari Repository
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess {
                ToastUtils.showSuccess(this, "Selamat datang kembali!")
                goToDashboard()
            }
            result.onFailure { error ->
                // Pesan error di sini sudah otomatis terfilter (Email tdk ada / Password salah)
                ToastUtils.showError(this, error.message ?: "Login gagal")
            }
        }

        viewModel.resetPasswordResult.observe(this) { result ->
            result.onSuccess { msg ->
                ToastUtils.showSuccess(this, msg)
            }
            result.onFailure { err ->
                ToastUtils.showError(this, "Gagal: ${err.message}")
            }
        }
    }

    private fun setupListeners() {
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            if (isFormValid()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (isValidEmail(email)) {
                DialogUtils.showCustomDialog(
                    context = this,
                    title = "Lupa Kata Sandi?",
                    message = "Kami akan mengirimkan link untuk mereset kata sandi ke email:\n$email",
                    positiveButtonText = "Kirim Link",
                    onConfirm = {
                        viewModel.resetPassword(email)
                    }
                )
            } else {
                ToastUtils.showError(this, "Mohon isi email Anda terlebih dahulu")
            }
        }
    }

    private fun setupLiveValidation() {
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
                binding.tilEmail.error = "Format email salah"
            } else {
                binding.tilEmail.error = null
            }
        }
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            binding.tilPassword.error = if ((text?.length ?: 0) < 1) "Kata sandi wajib diisi" else null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.tilEmail.error = "Mohon isi Email Anda"
            binding.etEmail.requestFocus()
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }

    private fun isFormValid(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) binding.tilEmail.error = "Email wajib diisi"
        if (password.isEmpty()) binding.tilPassword.error = "Kata sandi wajib diisi"

        return email.isNotEmpty() && password.isNotEmpty() && binding.tilEmail.error == null
    }

    private fun goToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "" else "Masuk Sekarang"
    }
}