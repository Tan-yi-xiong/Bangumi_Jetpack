package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository

class CategoryBangumisFactory(
    private val repository: HomeDataRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CategoryBangumisViewModel(repository) as T
    }
}