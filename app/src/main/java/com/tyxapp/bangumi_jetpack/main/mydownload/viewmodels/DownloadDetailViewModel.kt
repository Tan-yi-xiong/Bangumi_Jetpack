package com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.data.UIStata
import com.tyxapp.bangumi_jetpack.download.DownloadManager
import com.tyxapp.bangumi_jetpack.repository.DownloadDetailRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.launch

class DownloadDetailViewModel(
    private val repository: DownloadDetailRepository
) : ViewModel() {

    val uiStata = MutableLiveData<UIStata>()
    val alertMessage = MutableLiveData<String>()

    private val loadingData = MutableLiveData<Pair<String, String>>()

    val bangumiDownLoadVideos = loadingData.switchMap {
        uiStata.value = UIStata.LOADING
        val downLoadVideos = repository.getBangumiDownLoadVideo(it.first, it.second)
        downLoadVideos
    }

    fun loadingData(bangumiId: String, bangmiSource: String) {
        loadingData.value = Pair(bangumiId, bangmiSource)
    }

    /**
     * 下载详情条目长按事件
     *
     */
    fun onitemLongClick(id: Int) {
        viewModelScope.launch {
            val downLoadInfo = repository.getDownloadInfoBy(id)!!
            repository.removeTask(downLoadInfo)
            alertMessage.value = "${downLoadInfo.name} 已移除"
        }
    }
}