package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import kotlinx.coroutines.launch

class CategoryBangumisViewModel(
    private val repository: HomeDataRepository
) : ViewModel() {
    private val categoryWord = MutableLiveData<String>()

    private val listing = map(categoryWord) {
        repository.getCategoryBangumis(it)
    }

    val categoryBangumis = switchMap(listing) { it.liveDataPagelist }

    val initialLoad = switchMap(listing) { it.initialLoad }

    /**
     * 条目点击
     */
    val itemClickPosition = MutableLiveData<Int>()

    fun getCategoryBangumis(categoryWord: String) {
        if (this.categoryWord.value != categoryWord) {
            this.categoryWord.value = categoryWord
        }
    }


    fun retry() = viewModelScope.launch {
        listing.value?.retry?.invoke()
    }

    /**
     * 追番按钮(心性按钮)点击触发
     *
     */
    fun bangumiFollowStateChange(bangumi: Bangumi?, isFollow: Boolean) {
        bangumi ?: return
        viewModelScope.launch {
            repository.changeBangumiFollowState(bangumi, isFollow)
        }
    }

}
