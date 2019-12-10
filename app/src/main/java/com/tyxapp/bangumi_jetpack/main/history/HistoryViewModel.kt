package com.tyxapp.bangumi_jetpack.main.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.repository.HistoryWitchRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: HistoryWitchRepository
) : ViewModel() {

    val historyBangumis = LivePagedListBuilder(
        repository.getHistoryWitchBagnumis(),
        10
    ).build()

    val alertMessage = MutableLiveData<String>()

    fun onHistoryItemLongClick(bangumiDetail: BangumiDetail) {
        viewModelScope.launch {
            repository.remove(bangumiDetail)
            alertMessage.value = "${bangumiDetail.name}已移除出历史观看"
        }
    }

}
