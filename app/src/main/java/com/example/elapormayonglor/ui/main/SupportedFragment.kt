package com.example.elapormayonglor.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elapormayonglor.R
import com.example.elapormayonglor.data.repository.ReportRepository
import com.example.elapormayonglor.databinding.FragmentSupportedBinding
import com.example.elapormayonglor.ui.adapter.ReportAdapter
import com.example.elapormayonglor.ui.report.DetailReportActivity
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.ReportViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class SupportedFragment : Fragment() {

    private var _binding: FragmentSupportedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSupportedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObserver()
        setupListeners()

        // Load data laporan yang didukung oleh user saat ini
        viewModel.loadSupportedReports()
    }

    private fun setupViewModel() {
        val repository = ReportRepository()
        val factory = ViewModelFactory(reportRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // PERBAIKAN: Menggunakan Mode SUPPORTED dan menyertakan callback dukungan
        reportAdapter = ReportAdapter(
            mode = ReportAdapter.AdapterMode.SUPPORTED,
            onItemClick = { report ->
                val intent = Intent(requireContext(), DetailReportActivity::class.java)
                intent.putExtra("EXTRA_REPORT", report)
                startActivity(intent)
            },
            onSupportClick = { report ->
                // User bisa membatalkan dukungan langsung dari daftar ini
                viewModel.toggleSupport(report.reportId)
            },
            onEditClick = null,   // Mode Supported tidak butuh Edit
            onDeleteClick = null  // Mode Supported tidak butuh Delete
        )

        binding.rvSupported.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObserver() {
        // Observer untuk daftar laporan yang didukung
        viewModel.supportedReports.observe(viewLifecycleOwner) { list ->
            reportAdapter.submitList(list)

            if (list.isEmpty()) {
                binding.rvSupported.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvSupported.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }
        }

        // Observer untuk indikator loading (ProgressBar)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Pastikan di XML FragmentSupported ada ProgressBar dengan ID progressBar
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer untuk hasil operasi (Error Handling)
        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result.onFailure {
                ToastUtils.showError(requireActivity(), it.message ?: "Terjadi kesalahan")
            }
        }
    }

    private fun setupListeners() {
        // Tombol Explore jika list kosong, arahkan ke Home
        binding.btnExplore.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}