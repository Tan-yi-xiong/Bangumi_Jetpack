package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tyxapp.bangumi_jetpack.repository.BangumiFollowRepository

class BangumiFollowViewModelFactory(
    private val repository: BangumiFollowRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BangumiFollowViewModel(repository) as T
    }
}