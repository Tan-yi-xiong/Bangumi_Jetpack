package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.info
import kotlinx.coroutines.launch
import java.lang.Exception

class CategoryBangumisModel(
    private val repository: HomeDataRepository
) : ViewModel() {
    lateinit var categoryBangumis: LiveData<PagedList<Bangumi>>

    fun getCategoryBangumis(categoryWord: String) {
        categoryBangumis = LivePagedListBuilder(
            repository.getCategoryBangumis(categoryWord),
            PagedList.Config.Builder()
                .setPageSize(10)
                .build()
        ).build()
    }

    private fun errorHandle(e: Throwable) {
        info(e.toString())
    }

}
