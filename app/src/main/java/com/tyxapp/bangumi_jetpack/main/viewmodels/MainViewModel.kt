package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tyxapp.bangumi_jetpack.data.HomeDataRepository

class MainViewModel : ViewModel() {
    val homeDataRepository = MutableLiveData<HomeDataRepository>()
}