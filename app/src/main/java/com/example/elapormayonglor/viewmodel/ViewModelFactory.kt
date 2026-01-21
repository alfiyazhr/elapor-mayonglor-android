package com.example.elapormayonglor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elapormayonglor.data.repository.AuthRepository
import com.example.elapormayonglor.data.repository.ReportRepository

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val reportRepository: ReportRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository!!) as T
        } else if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(reportRepository!!) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}