package com.example.elapormayonglor.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import com.example.elapormayonglor.R
import com.google.android.material.button.MaterialButton

object DialogUtils {

    fun showCustomDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "Ya",
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirmation, null)
        dialog.setContentView(view)

        // Bikin background dialog transparan biar rounded corners-nya kelihatan
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Bikin dialog lebar (match parent dengan margin)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Binding View
        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)

        // Set Data
        tvTitle.text = title
        tvMessage.text = message
        btnConfirm.text = positiveButtonText

        // Set Listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onConfirm() // Jalankan aksi (Logout / Reset Pass)
        }

        dialog.show()
    }
}