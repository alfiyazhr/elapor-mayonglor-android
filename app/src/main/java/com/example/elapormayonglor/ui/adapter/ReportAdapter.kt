package com.example.elapormayonglor.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elapormayonglor.R
import com.example.elapormayonglor.data.model.Report
import com.example.elapormayonglor.databinding.ItemReportBinding
import com.example.elapormayonglor.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth

class ReportAdapter(
    private val mode: AdapterMode, // Mode: HOME, HISTORY, atau SUPPORTED
    private val onItemClick: (Report) -> Unit,
    private val onEditClick: ((Report) -> Unit)? = null,
    private val onDeleteClick: ((Report) -> Unit)? = null,
    private val onSupportClick: ((Report) -> Unit)? = null
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(DiffCallback) {

    enum class AdapterMode { HOME, HISTORY, SUPPORTED }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid

            binding.apply {
                // Set Data Dasar
                tvTitle.text = report.judul
                tvCategory.text = report.kategori
                tvLocation.text = report.alamatLokasi
                tvDate.text = DateUtils.formatToDisplay(report.createdAt, isDetail = false)

                // Logika Warna Status
                tvStatus.text = report.status
                val statusColor = when (report.status) {
                    "Selesai" -> R.color.status_success
                    "Ditolak" -> R.color.status_error
                    "Diproses" -> R.color.brand_secondary
                    else -> R.color.brand_primary
                }
                tvStatus.setTextColor(ContextCompat.getColor(root.context, statusColor))

                // --- LOGIKA VISIBILITY BERDASARKAN MODE (REVISI) ---
                when (mode) {
                    AdapterMode.HOME -> {
                        // REVISI: Home dibuat bersih total tanpa tombol aksi
                        layoutActions.visibility = View.GONE
                    }

                    AdapterMode.HISTORY -> {
                        // History: Munculkan Edit/Hapus jika milik sendiri & status masih 'Terkirim'
                        if (report.userId == currentUid && report.status.equals("Terkirim", ignoreCase = true)) {
                            layoutActions.visibility = View.VISIBLE
                            btnEdit.visibility = View.VISIBLE
                            btnDelete.visibility = View.VISIBLE
                            btnSupportToggle.visibility = View.GONE
                        } else {
                            layoutActions.visibility = View.GONE
                        }
                    }

                    AdapterMode.SUPPORTED -> {
                        // Supported: Hanya munculkan tombol batal dukung (Heart icon)
                        layoutActions.visibility = View.VISIBLE
                        btnEdit.visibility = View.GONE
                        btnDelete.visibility = View.GONE
                        btnSupportToggle.visibility = View.VISIBLE

                        // Logika Toggle Icon Dukung
                        val isSupported = report.pendukung.contains(currentUid)
                        btnSupportToggle.setImageResource(if (isSupported) R.drawable.ic_dukung else R.drawable.ic_dukung_outline)

                        // Warna merah jika didukung, abu-abu jika tidak
                        val tintColor = if (isSupported) R.color.green_supported else R.color.gray_text
                        btnSupportToggle.imageTintList = ContextCompat.getColorStateList(root.context, tintColor)
                    }
                }

                // --- LISTENERS ---
                btnEdit.setOnClickListener { onEditClick?.invoke(report) }
                btnDelete.setOnClickListener { onDeleteClick?.invoke(report) }
                btnSupportToggle.setOnClickListener { onSupportClick?.invoke(report) }

                // Klik pada seluruh kartu untuk ke Detail
                root.setOnClickListener { onItemClick(report) }

                // Load Gambar Laporan
                Glide.with(root.context)
                    .load(report.fotoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_header_gradient)
                    .into(ivReportImage)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.reportId == newItem.reportId
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
}