package com.example.elapormayonglor.utils

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.elapormayonglor.R

object ToastUtils {

    fun showSuccess(activity: Activity, message: String) {
        showCustomToast(activity, message, true)
    }

    fun showError(activity: Activity, message: String) {
        showCustomToast(activity, message, false)
    }

    private fun showCustomToast(activity: Activity, message: String, isSuccess: Boolean) {
        try {
            // Gunakan layoutInflater dari activity agar lebih sinkron dengan tema UI
            val inflater = activity.layoutInflater
            val layout = inflater.inflate(R.layout.layout_top_toast, null)

            val cvIconContainer = layout.findViewById<CardView>(R.id.cvIconContainer)
            val ivIcon = layout.findViewById<ImageView>(R.id.ivToastIcon)
            val tvMessage = layout.findViewById<TextView>(R.id.tvToastMessage)

            tvMessage.text = message

            if (isSuccess) {
                // Style SUKSES (Hijau)
                cvIconContainer.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                ivIcon.setImageResource(R.drawable.ic_success)
                // Mengambil warna brand_primary kamu biar biru cakep kalau sukses
                ivIcon.setColorFilter(ContextCompat.getColor(activity, R.color.brand_primary))
            } else {
                // Style ERROR (Merah)
                cvIconContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                ivIcon.setImageResource(R.drawable.ic_error)
                ivIcon.setColorFilter(Color.parseColor("#F44336"))
            }

            val toast = Toast(activity.applicationContext)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout

            // Posisi 150 agar tidak tertutup notch atau status bar pada HP modern
            toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 150)
            toast.show()
        } catch (e: Exception) {
            // Fallback: Jika custom toast gagal karena activity hancur, pakai toast standar biar tidak crash
            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
}