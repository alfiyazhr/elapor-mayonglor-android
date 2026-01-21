package com.example.elapormayonglor.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elapormayonglor.utils.ToastUtils

// Open class artinya bisa diwariskan ke activity lain
open class BaseActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isFirstLoad = true // Biar gak muncul toast "Online" pas baru buka aplikasi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Setup Callback Jaringan
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            // Kalo Internet Nyala/Balik
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    if (!isFirstLoad) {
                        ToastUtils.showSuccess(this@BaseActivity, "Koneksi internet terhubung kembali.")
                    }
                    isFirstLoad = false
                }
            }

            // Kalo Internet Mati/Ilang
            override fun onLost(network: Network) {
                super.onLost(network)
                runOnUiThread {
                    isFirstLoad = false // Set false biar pas nyala lagi dia notif
                    ToastUtils.showError(this@BaseActivity, "Anda sedang offline. Periksa koneksi internet.")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Mulai memantau saat activity aktif
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onPause() {
        super.onPause()
        // Berhenti memantau saat activity tidak aktif (biar hemat baterai)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}