package com.tyxapp.bangumi_jetpack.main.search.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    // 执行搜索的词
    val searchWord = MutableLiveData<String>()
}