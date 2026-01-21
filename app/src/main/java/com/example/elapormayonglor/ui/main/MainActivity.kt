package com.example.elapormayonglor.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.elapormayonglor.R
import com.example.elapormayonglor.databinding.ActivityMainBinding
import com.example.elapormayonglor.ui.report.CreateReportActivity
import com.example.elapormayonglor.ui.BaseActivity

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Load Home Default
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 2. Setup Bottom Nav
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_history -> replaceFragment(HistoryFragment())
                R.id.nav_supported -> replaceFragment(SupportedFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }

        // 3. Setup FAB
        binding.fabCreateReport.setOnClickListener {
            startActivity(Intent(this, CreateReportActivity::class.java))
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        if (fragment is HomeFragment) {
            binding.fabCreateReport.show()
        } else {
            binding.fabCreateReport.hide()
        }
    }
}