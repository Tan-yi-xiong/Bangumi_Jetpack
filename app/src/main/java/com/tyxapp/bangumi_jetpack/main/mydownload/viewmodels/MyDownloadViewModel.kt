package com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.repository.MyDownloadRepository
import kotlinx.coroutines.launch

class MyDownloadViewModel(
    private val repository: MyDownloadRepository
) : ViewModel() {
    val downLoadBangumis: LiveData<List<BangumiDetail>> by lazy(LazyThreadSafetyMode.NONE) {
        repository.getDownloadBangumi()
    }

    val alertMessage = MutableLiveData<String>()

    fun onItemLongCick(bangumiDetail: BangumiDetail) {
        viewModelScope.launch {
            repository.removeDownloadBangmi(bangumiDetail)
            alertMessage.value = "${bangumiDetail.name} 已移除"
        }
    }
}
