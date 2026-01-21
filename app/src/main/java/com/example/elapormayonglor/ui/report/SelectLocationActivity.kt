package com.example.elapormayonglor.ui.report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.elapormayonglor.R
import com.example.elapormayonglor.databinding.ActivitySelectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectLocationBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnConfirmLocation.setOnClickListener {
            val center: LatLng = mMap.cameraPosition.target
            val resultIntent = Intent()
            resultIntent.putExtra("latitude", center.latitude)
            resultIntent.putExtra("longitude", center.longitude)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Titik awal Mayong Lor (Sesuaikan koordinat aslinya)
        val mayongLor = LatLng(-6.6631, 110.7719)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mayongLor, 15f))
    }
}