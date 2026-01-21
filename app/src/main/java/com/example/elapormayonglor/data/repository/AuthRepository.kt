package com.example.elapormayonglor.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.elapormayonglor.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun setLoggedIn(isLoggedIn: Boolean) {
        editor.putBoolean("is_logged_in", isLoggedIn)
        editor.apply()
    }

    fun isUserLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    /**
     * PERBAIKAN: Fungsi Login Direct (Tanpa Pre-check yang bikin bug)
     */
    fun login(email: String, pass: String, callback: (Result<AuthResult>) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        callback(Result.success(task.result!!))
                    } else {
                        auth.signOut()
                        callback(Result.failure(Exception("Login Gagal: Email belum diverifikasi. Silakan cek inbox/spam email Anda.")))
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        // Email benar-benar tidak ada di Firebase Auth
                        is FirebaseAuthInvalidUserException -> {
                            "Login Gagal: Email belum terdaftar. Silakan buat akun terlebih dahulu."
                        }
                        // Password salah ATAU format email salah
                        is FirebaseAuthInvalidCredentialsException -> {
                            "Login Gagal: Kata sandi salah atau tidak sesuai."
                        }
                        is FirebaseAuthException -> {
                            when (exception.errorCode) {
                                "ERROR_NETWORK_REQUEST_FAILED" -> "Koneksi internet bermasalah."
                                "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti."
                                else -> "Login gagal: ${exception.localizedMessage}"
                            }
                        }
                        else -> exception?.localizedMessage ?: "Terjadi kesalahan sistem."
                    }
                    callback(Result.failure(Exception(errorMessage)))
                }
            }
    }

    fun logout() {
        auth.signOut()
        setLoggedIn(false)
    }

    fun resetPassword(email: String): Task<Void> = auth.sendPasswordResetEmail(email)

    fun getCurrentUser() = auth.currentUser

    fun getUserProfileByUid(uid: String) = db.collection("users")
        .whereEqualTo("uid", uid)
        .get()

    fun register(email: String, pass: String): Task<AuthResult> = auth.createUserWithEmailAndPassword(email, pass)

    fun sendEmailVerification(): Task<Void>? = auth.currentUser?.sendEmailVerification()

    fun saveUserToFirestore(user: User): Task<Void> = db.collection("users").document(user.nik).set(user)

    fun checkNikExists(nik: String): Task<DocumentSnapshot> = db.collection("users").document(nik).get()
}