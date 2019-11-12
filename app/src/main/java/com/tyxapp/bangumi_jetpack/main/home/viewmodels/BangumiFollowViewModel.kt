package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.repository.BangumiFollowRepository
import kotlinx.coroutines.launch

class BangumiFollowViewModel(
    private val repository: BangumiFollowRepository
) : ViewModel() {
    val followBangumis = LivePagedListBuilder(repository.getFollowBangumis(), 10).build()
    val removeMessage = MutableLiveData<String>() //移除提醒消息

    /**
     * 列表长按事件, 移除出追番列表
     *
     */
    fun onItemLongClick(bangumiDetail: BangumiDetail) {
        viewModelScope.launch {
            repository.removeFromFollow(bangumiDetail)
            removeMessage.value = "${bangumiDetail.name} 已移除出追番表"
        }
    }
}
