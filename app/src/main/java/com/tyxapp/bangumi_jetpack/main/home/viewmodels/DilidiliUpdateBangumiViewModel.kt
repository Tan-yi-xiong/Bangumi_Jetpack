package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.InitialLoad
import com.tyxapp.bangumi_jetpack.data.Listing
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.launch

class DilidiliUpdateBangumiViewModel (
    private val repository: HomeDataRepository
) : ViewModel() {
    private val listing = MutableLiveData<Listing<Bangumi>>()
    val bangumisLiveData: LiveData<PagedList<Bangumi>> = listing.switchMap { it.liveDataPagelist }
    val initialState: LiveData<InitialLoad> = listing.switchMap { it.initialLoad }

    fun loadData() {
        listing.value = repository.getCategoryBangumis("最新更新")
    }

    fun retry() {
        viewModelScope.launch {
            listing.value?.retry?.invoke()
        }
    }
}