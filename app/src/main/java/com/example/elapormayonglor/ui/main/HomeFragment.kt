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
import com.example.elapormayonglor.databinding.FragmentHomeBinding
import com.example.elapormayonglor.ui.adapter.ReportAdapter
import com.example.elapormayonglor.ui.report.DetailReportActivity
import com.example.elapormayonglor.utils.ToastUtils
import com.example.elapormayonglor.viewmodel.ReportViewModel
import com.example.elapormayonglor.viewmodel.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReportViewModel
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObserver()
        setupClickListeners()
        setupSwipeRefresh()

        // Load data pertama kali
        viewModel.loadAllReports()
    }

    private fun setupViewModel() {
        val repository = ReportRepository()
        val factory = ViewModelFactory(reportRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ReportViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // PERBAIKAN: Menggunakan Mode HOME dan menambahkan callback onSupportClick
        reportAdapter = ReportAdapter(
            mode = ReportAdapter.AdapterMode.HOME,
            onItemClick = { report ->
                val intent = Intent(requireContext(), DetailReportActivity::class.java)
                intent.putExtra("EXTRA_REPORT", report)
                startActivity(intent)
            },
            onSupportClick = { report ->
                // User bisa langsung dukung/batal dukung dari Home
                viewModel.toggleSupport(report.reportId)
            }
        )

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObserver() {
        viewModel.homeReports.observe(viewLifecycleOwner) { list ->
            reportAdapter.submitList(list)

            // Tampilkan atau sembunyikan empty state jika perlu
            if (list.isEmpty()) {
                binding.layoutEmptyHome?.visibility = View.VISIBLE
            } else {
                binding.layoutEmptyHome?.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result.onFailure {
                ToastUtils.showError(requireActivity(), it.message ?: "Gagal memproses data")
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary)
        binding.swipeRefresh.setOnRefreshListener {
            // Reset filter ke "Semua"
            if (binding.chipGroupCategory.childCount > 0) {
                val chipAll = binding.chipGroupCategory.getChildAt(0) as? Chip
                chipAll?.isChecked = true
            }
            viewModel.loadAllReports()
        }
    }

    private fun setupClickListeners() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                viewModel.filterHomeByCategory(chip.text.toString())
            } else {
                viewModel.filterHomeByCategory("Semua")
            }
        }

        binding.cvProfile.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNav.selectedItemId = R.id.nav_profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}