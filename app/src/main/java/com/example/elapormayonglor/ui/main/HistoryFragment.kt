package com.example.elapormayonglor.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elapormayonglor.R
import com.example.elapormayonglor.data.model.Report
import com.example.elapormayonglor.data.repository.ReportRepository
import com.example.elapormayonglor.databinding.FragmentHistoryBinding
import com.example.elapormayonglor.ui.adapter.ReportAdapter
import com.example.elapormayonglor.ui.report.CreateReportActivity
import com.example.elapormayonglor.ui.report.DetailReportActivity
import com.example.elapormayonglor.utils.DialogUtils
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.ReportViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.google.android.material.chip.Chip

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel
    private lateinit var reportAdapter: ReportAdapter

    // Launcher untuk refresh data otomatis setelah kembali dari Edit Laporan
    private val editReportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadMyReports() // Refresh data otomatis jika ada perubahan (Edit Berhasil)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObserver()
        setupClickListeners()
        applyFontToChips()

        // Ambil data laporan milik user sendiri
        viewModel.loadMyReports()
    }

    private fun setupViewModel() {
        val repository = ReportRepository()
        val factory = ViewModelFactory(reportRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Menggunakan Mode HISTORY agar tombol Edit/Delete aktif di item_report
        reportAdapter = ReportAdapter(
            mode = ReportAdapter.AdapterMode.HISTORY,
            onItemClick = { report ->
                val intent = Intent(requireContext(), DetailReportActivity::class.java)
                intent.putExtra("EXTRA_REPORT", report)
                startActivity(intent)
            },
            onEditClick = { report ->
                // Proteksi: Edit hanya boleh jika belum diproses admin (Status Terkirim)
                if (report.status.equals("Terkirim", ignoreCase = true)) {
                    val intent = Intent(requireContext(), CreateReportActivity::class.java)
                    intent.putExtra("EXTRA_REPORT", report)
                    editReportLauncher.launch(intent)
                } else {
                    ToastUtils.showError(requireActivity(), "Laporan sedang diproses, tidak bisa diedit")
                }
            },
            onDeleteClick = { report ->
                showDeleteConfirmation(report)
            },
            onSupportClick = null // Di History tidak perlu tombol dukung
        )

        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
            setHasFixedSize(true)
        }
    }

    // --- REVISI: Menggunakan DialogUtils milikmu ---
    private fun showDeleteConfirmation(report: Report) {
        DialogUtils.showCustomDialog(
            context = requireContext(),
            title = "Hapus Laporan?",
            message = "Apakah kamu yakin ingin menghapus laporan '${report.judul}'? Data akan hilang permanen.",
            positiveButtonText = "Hapus",
            onConfirm = {
                // Jalankan proses hapus data di Firestore & foto di Storage
                viewModel.deleteReport(report.reportId, report.fotoUrl)
            }
        )
    }

    private fun setupObserver() {
        // Pantau loading untuk SwipeRefresh
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            _binding?.swipeRefresh?.isRefreshing = isLoading
        }

        // Pantau list laporan saya
        viewModel.historyReports.observe(viewLifecycleOwner) { list ->
            reportAdapter.submitList(list)

            // Tampilkan Empty State jika tidak ada riwayat
            if (list.isEmpty()) {
                binding.rvHistory.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvHistory.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }
        }

        // Pantau hasil operasi (Sukses/Gagal Hapus atau Update)
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { message ->
                ToastUtils.showSuccess(requireActivity(), message)
                // Refresh list agar tampilan sinkron setelah hapus
                viewModel.loadMyReports()
            }
            result.onFailure { error ->
                ToastUtils.showError(requireActivity(), error.message ?: "Terjadi kesalahan")
            }
        }
    }

    private fun setupClickListeners() {
        // Swipe Down Refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadMyReports()
        }

        // Filter Status Chip
        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                viewModel.filterHistoryByStatus(chip.text.toString())
            } else {
                viewModel.filterHistoryByStatus("Semua")
            }
        }

        // Tombol "Buat Laporan" dari Empty State
        binding.btnCreateReport.setOnClickListener {
            val intent = Intent(requireContext(), CreateReportActivity::class.java)
            startActivity(intent)
        }
    }

    private fun applyFontToChips() {
        try {
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins)
            for (i in 0 until binding.chipGroupStatus.childCount) {
                val chip = binding.chipGroupStatus.getChildAt(i) as? Chip
                chip?.typeface = typeface
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}