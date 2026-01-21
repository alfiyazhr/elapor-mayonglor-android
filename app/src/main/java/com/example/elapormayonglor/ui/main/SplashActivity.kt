package com.example.elapormayonglor.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.elapormayonglor.R
import com.example.elapormayonglor.data.repository.AuthRepository
import com.example.elapormayonglor.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1. Atur Warna Status Bar
        try {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = android.graphics.Color.parseColor("#134574")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Timer & Logika "Double Check"
        Handler(Looper.getMainLooper()).postDelayed({
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val authRepository = AuthRepository(this)

            // LOGIKA PENTING:
            // Hanya masuk Beranda jika Firebase User ADA dan Repository bilang SUDAH LOGIN.
            // Jika aplikasi baru diinstall, Repository pasti FALSE -> Maka masuk ke ELSE.
            if (user != null && authRepository.isUserLoggedIn()) {
                // User Valid -> Ke Beranda
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                // User Tidak Valid / Habis Install Ulang -> Paksa Logout Bersih -> Ke Login
                auth.signOut() // Hapus sesi hantu
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            finish()
        }, 3000)
    }
}