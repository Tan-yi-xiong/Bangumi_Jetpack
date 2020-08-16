package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.TimeTableBangumi
import com.tyxapp.bangumi_jetpack.data.UIStata
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.launch

class TimeTableViewModel(
    private var mRepository: HomeDataRepository
) : ViewModel() {

    val timeTableBagnumisLiveData = MutableLiveData<List<List<TimeTableBangumi>>>()

    val uiStata = MutableLiveData<UIStata>()

    fun getTimeTableBagnumis() = viewModelScope.launch {
        try {
            uiStata.value = UIStata.LOADING
            timeTableBagnumisLiveData.value = mRepository.getBangumiTimeTable()

            uiStata.value = UIStata.SUCCESS
        } catch (e: Exception) {
            errorHandle(e)
        }
    }

    fun replaceRepository(repository: HomeDataRepository) {
        if (mRepository != repository) {
            mRepository = repository
            getTimeTableBagnumis()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            getTimeTableBagnumis()
        }
    }

    private fun errorHandle(e: Exception) {
        LOGI(e.toString())
        e.printStackTrace()
        uiStata.value = UIStata.error(e.toString())
    }


}
