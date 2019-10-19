package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    val searchWord = MutableLiveData<String>()
}