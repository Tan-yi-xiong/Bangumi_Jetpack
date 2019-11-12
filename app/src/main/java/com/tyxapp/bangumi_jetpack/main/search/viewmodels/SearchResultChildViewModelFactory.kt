package com.tyxapp.bangumi_jetpack.main.search.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tyxapp.bangumi_jetpack.repository.SearchResultChildRepository

class SearchResultChildViewModelFactory(
    private val repository: SearchResultChildRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchResultChildViewModel(
            repository
        ) as T
    }
}