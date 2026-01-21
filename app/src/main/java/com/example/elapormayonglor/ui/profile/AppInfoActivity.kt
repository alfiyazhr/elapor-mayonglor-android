package com.example.elapormayonglor.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elapormayonglor.databinding.ActivityAppInfoBinding

class AppInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Tombol Back di Toolbar
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 2. Tombol Email (Klik langsung buka Gmail)
        binding.btnEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:admpemdesmayonglor@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Tanya Admin E-Lapor")
            }
            // Cek dulu apakah ada aplikasi email di HP user
            try {
                startActivity(emailIntent)
            } catch (e: Exception) {
                // Kalau gak ada aplikasi email (jarang terjadi), biarin aja atau kasih Toast
            }
        }
    }
}