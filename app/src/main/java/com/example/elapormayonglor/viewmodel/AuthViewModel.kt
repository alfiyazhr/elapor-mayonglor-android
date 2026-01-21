package com.example.elapormayonglor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elapormayonglor.data.model.User
import com.example.elapormayonglor.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Menggunakan Result<String> agar Activity bisa menangkap pesan sukses/gagal yang spesifik
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult: LiveData<Result<String>> = _registerResult

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _resetPasswordResult = MutableLiveData<Result<String>>()
    val resetPasswordResult: LiveData<Result<String>> = _resetPasswordResult

    // --- SESSION & USER INFO ---
    fun getCurrentUserEmail(): String? = repository.getCurrentUser()?.email

    fun checkLoginStatus(): Boolean {
        val user = repository.getCurrentUser()
        // Cek apakah user ada, sudah verifikasi email, dan session di prefs TRUE
        return user != null && user.isEmailVerified && repository.isUserLoggedIn()
    }

    // =========================================================================
    //  LOGIN (EXPERT VERSION)
    // =========================================================================
    fun login(email: String, pass: String) {
        _isLoading.value = true

        // Memanggil fungsi login baru dari Repository yang sudah memfilter error
        repository.login(email, pass) { result ->
            _isLoading.value = false
            result.onSuccess {
                // Jika sukses, status login disimpan ke SharedPreferences
                repository.setLoggedIn(true)
                _loginResult.value = Result.success("Login Berhasil!")
            }
            result.onFailure { e ->
                // Pesan error spesifik (Email tdk ada / Password salah) diteruskan ke UI
                _loginResult.value = Result.failure(e)
            }
        }
    }

    // =========================================================================
    //  REGISTER (NIK -> EMAIL)
    // =========================================================================
    fun register(nik: String, nama: String, email: String, pass: String, alamat: String) {
        _isLoading.value = true

        // 1. CEK NIK DI FIRESTORE TERLEBIH DAHULU
        repository.checkNikExists(nik).addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.exists() == true) {
                _isLoading.value = false
                _registerResult.value = Result.failure(Exception("NIK ini sudah terdaftar. Gunakan NIK lain atau hubungi Admin."))
            } else {
                // 2. NIK AMAN, LANJUT PROSES AUTH REGISTER
                processAuthRegister(nik, nama, email, pass, alamat)
            }
        }
    }

    private fun processAuthRegister(nik: String, nama: String, email: String, pass: String, alamat: String) {
        repository.register(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                // 3. Kirim Email Verifikasi
                repository.sendEmailVerification()?.addOnCompleteListener {
                    val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                    val newUser = User(
                        nik = nik,
                        nama = nama,
                        email = email,
                        alamat = alamat,
                        role = "warga",
                        uid = uid,
                        createdAt = currentDate
                    )
                    // 4. Simpan ke Database Profil (Firestore)
                    saveUserToFirestore(newUser)
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "Email sudah digunakan oleh akun lain."
                    else -> e.message ?: "Gagal melakukan registrasi."
                }
                _registerResult.value = Result.failure(Exception(errorMessage))
            }
    }

    private fun saveUserToFirestore(user: User) {
        repository.saveUserToFirestore(user)
            .addOnSuccessListener {
                // Logout sementara agar user harus login manual setelah verifikasi email
                repository.logout()
                _isLoading.value = false
                _registerResult.value = Result.success("Registrasi berhasil! Silakan verifikasi email Anda sebelum login.")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _registerResult.value = Result.failure(Exception("Data profil gagal disimpan: ${e.message}"))
            }
    }

    // =========================================================================
    //  PROFILE & PASSWORD RESET
    // =========================================================================
    fun loadUserProfile() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            repository.getUserProfileByUid(currentUser.uid)
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDoc = querySnapshot.documents[0]
                        val fullUser = userDoc.toObject(User::class.java)
                        _userProfile.value = fullUser
                    }
                }
        }
    }

    fun resetPassword(email: String) {
        _isLoading.value = true
        repository.resetPassword(email)
            .addOnSuccessListener {
                _isLoading.value = false
                _resetPasswordResult.value = Result.success("Link reset telah dikirim ke $email")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _resetPasswordResult.value = Result.failure(Exception("Gagal mengirim link: ${e.message}"))
            }
    }

    fun logout() = repository.logout()
}