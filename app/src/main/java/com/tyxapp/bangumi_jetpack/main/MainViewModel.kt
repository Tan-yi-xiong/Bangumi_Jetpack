package com.tyxapp.bangumi_jetpack.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository

class MainViewModel : ViewModel() {
    // 主页的FragmentViewmodel共享的Repository
    val homeDataRepository = MutableLiveData<HomeDataRepository>()
}