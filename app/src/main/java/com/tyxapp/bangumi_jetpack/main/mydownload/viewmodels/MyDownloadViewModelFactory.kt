package com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tyxapp.bangumi_jetpack.repository.MyDownloadRepository

class MyDownloadViewModelFactory(
    private val myDownloadRepository: MyDownloadRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MyDownloadViewModel(
            myDownloadRepository
        ) as T
    }
}