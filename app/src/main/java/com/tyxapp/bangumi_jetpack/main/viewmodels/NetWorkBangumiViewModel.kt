package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.info
import kotlinx.coroutines.launch
import java.lang.Exception

class NetWorkBangumiViewModel(private val homeDataRepository: HomeDataRepository) : ViewModel() {
    val bangumis: MutableLiveData<Map<String, List<Bangumi>>>  by lazy {
        MutableLiveData<Map<String, List<Bangumi>>>().apply {
            getBngumis()
        }
    }

    val refreshing = MutableLiveData<Boolean>()

    val error = MutableLiveData<Throwable>()

    val empty = MutableLiveData<Boolean>()

    val firstLoading = MutableLiveData<Boolean>()

    private fun getBngumis() = luanch({
        firstLoading.value = true
        val data = homeDataRepository.getHomeBangumis()
        if (data.isEmpty()) {
            empty.value = true
        } else {
            bangumis.value = data
        }
        firstLoading.value = false
    })

    fun refresh() = luanch({
        refreshing.value = true
        val data = homeDataRepository.refreshHomeBangumis()
        if (data.isEmpty()) {
            empty.value = true
        } else {
            bangumis.value = data
        }
        refreshing.value = false
    })

    private fun errorHandle(throwable: Throwable) {
        error.value = throwable
        if (firstLoading.value == true) {
            firstLoading.value = false
        }
        if (refreshing.value == true) {
            refreshing.value = false
        }
    }


    private fun luanch(
            action: suspend () -> Unit,
            errorAction: (Throwable) -> Unit = ::errorHandle
    ) = viewModelScope.launch {
        try {
            action()
        } catch (e: Exception) {
            errorAction(e)
        }
    }
}
