package com.example.elapormayonglor.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.elapormayonglor.data.repository.AuthRepository
import com.example.elapormayonglor.databinding.FragmentProfileBinding
import com.example.elapormayonglor.ui.auth.LoginActivity
import com.example.elapormayonglor.ui.profile.AppInfoActivity
import com.example.elapormayonglor.utils.DialogUtils
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.AuthViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = AuthRepository(requireContext())
        val factory = ViewModelFactory(authRepository = repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupObservers()
        setupClickListeners()

        // Panggil ini untuk ambil data terbaru dari Firestore
        viewModel.loadUserProfile()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.nama
                binding.tvEmail.text = it.email

                // Gunakan Elvis Operator (?:) agar jika data null, UI tidak berantakan atau kosong
                binding.tvNik.text = "NIK: ${it.nik.ifEmpty { "-" }}"
                binding.tvAddress.text = "Alamat: ${it.alamat.ifEmpty { "-" }}"
            }
        }

        viewModel.resetPasswordResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { msg -> ToastUtils.showSuccess(requireActivity(), msg) }
            result.onFailure { err -> ToastUtils.showError(requireActivity(), err.message ?: "Terjadi kesalahan") }
        }

        // Tambahan: Observer Loading (Opsional tapi bagus agar user tahu data sedang dimuat)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Anda bisa tambahkan ProgressBar di XML jika mau,
            // tapi karena datanya kecil, biasanya load-nya instan.
        }
    }

    private fun setupClickListeners() {
        // Logout via Icon di Header (Sekarang sudah sinkron dengan XML baru)
        binding.btnLogout.setOnClickListener {
            DialogUtils.showCustomDialog(
                context = requireContext(),
                title = "Keluar Aplikasi",
                message = "Apakah Anda yakin ingin keluar?",
                positiveButtonText = "Ya, Keluar",
                onConfirm = {
                    viewModel.logout()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            )
        }

        binding.btnChangePassword.setOnClickListener {
            val email = viewModel.getCurrentUserEmail()
            if (!email.isNullOrEmpty()) {
                DialogUtils.showCustomDialog(
                    context = requireContext(),
                    title = "Ganti Kata Sandi",
                    message = "Kami akan mengirimkan link reset password ke email $email. Lanjutkan?",
                    positiveButtonText = "Kirim Link",
                    onConfirm = { viewModel.resetPassword(email) }
                )
            } else {
                ToastUtils.showError(requireActivity(), "Email tidak ditemukan")
            }
        }

        binding.btnHelp.setOnClickListener {
            startActivity(Intent(requireContext(), AppInfoActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}