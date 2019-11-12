package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.UIStata
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.launch

class NetWorkBangumiViewModel(
    private var homeDataRepository: HomeDataRepository
) : ViewModel() {

    val bangumis by lazy(LazyThreadSafetyMode.NONE) {
        MutableLiveData<Map<String, List<Bangumi>>>().apply {
            loadBangmis()
        }
    }

    val uiState = MutableLiveData<UIStata>()

    private fun loadBangmis() = luanch({
        uiState.value = UIStata.LOADING
        getBngumis()
        uiState.value = UIStata.SUCCESS
    })

    fun refresh() = luanch({
        if (uiState.value == UIStata.LOADING) {//加载状态下不执行刷新
            uiState.value = UIStata.refreshing(false)
        } else {
            uiState.value = UIStata.refreshing(true)
            getBngumis()
            uiState.value = UIStata.refreshing(false)
        }
    })

    private suspend fun getBngumis() {
        val data = homeDataRepository.getHomeBangumis()
        if (data.isEmpty()) {
            uiState.value = UIStata.DATA_EMPTY
        } else {
            bangumis.value = data
        }
    }

    fun replaceRepository(repository: HomeDataRepository) {
        homeDataRepository = repository
        luanch({
            loadBangmis()
        })
    }

    private fun errorHandle(throwable: Throwable) {
        LOGI("$throwable  NETViwemodel")
        uiState.value = UIStata.error(throwable.toString())
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
