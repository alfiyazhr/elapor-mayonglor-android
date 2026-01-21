package com.example.elapormayonglor.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    // Format asli yang kamu simpan di Firestore (dd-MM-yyyy HH:mm)
    private const val FORMAT_DATABASE = "dd-MM-yyyy HH:mm"

    // Format untuk Tampilan List (Ringkas): 23 Nov 2025, 15:02 WIB
    private const val FORMAT_UI_LIST = "dd MMM yyyy • HH:mm"

    // Format untuk Tampilan Detail (Lengkap): Senin, 23 November 2025 • 15:02 WIB
    private const val FORMAT_UI_DETAIL = "EEEE, dd MMMM yyyy • HH:mm"

    fun formatToDisplay(rawDate: String?, isDetail: Boolean = false): String {
        if (rawDate.isNullOrEmpty()) return "-"

        return try {
            // 1. Ubah String database jadi object Date
            val inputFormat = SimpleDateFormat(FORMAT_DATABASE, Locale.getDefault())
            val date = inputFormat.parse(rawDate) ?: return rawDate

            // 2. Tentukan mau format List atau Detail
            val pattern = if (isDetail) FORMAT_UI_DETAIL else FORMAT_UI_LIST

            // 3. Ubah object Date jadi String baru yang cantik (Pakai Locale Indonesia)
            val outputFormat = SimpleDateFormat(pattern, Locale("id", "ID"))

            // 4. Tambahkan "WIB" manual biar tegas
            "${outputFormat.format(date)} WIB"

        } catch (e: Exception) {
            // Kalau error parsing, balikin aja text aslinya biar gak crash
            rawDate
        }
    }
}