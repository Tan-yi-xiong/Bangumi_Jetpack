package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tyxapp.bangumi_jetpack.data.SearchHelperRepository

class SearchHelperViewModelFactory (
    private val repository: SearchHelperRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchHelperViewModel(repository) as T
    }
}