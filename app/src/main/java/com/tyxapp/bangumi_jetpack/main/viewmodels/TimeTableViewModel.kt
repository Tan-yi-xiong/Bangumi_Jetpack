package com.tyxapp.bangumi_jetpack.main.viewmodels

import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.info
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class TimeTableViewModel(
        private val repository: HomeDataRepository
) : ViewModel() {

    private lateinit var timeTableBagnumis: List<List<Bangumi>>
    val currentWeekDayValue = MutableLiveData<Int>()

    val error = MutableLiveData<Throwable>()
    val loading = MutableLiveData<Boolean>()
    val dayBangumis: MutableLiveData<List<Bangumi>> by lazy {
        MutableLiveData<List<Bangumi>>().apply {
            getTimeTableBagnumis()
        }
    }
    val firstLoadingFinish = MutableLiveData<Boolean>()

    private fun getTimeTableBagnumis() = viewModelScope.launch {
        try {
            loading.value = true
            timeTableBagnumis = repository.getBangumiTimeTable()
            if (currentWeekDayValue.value == null) {
                val today = getTodayWeekValue()
                currentWeekDayValue.value = today
                dayBangumis.value = timeTableBagnumis[today]
            }
            firstLoadingFinish.value = true
            loading.value = false
        } catch (e: Exception) {
            errorHandle(e)
        }
    }

    fun onBangumiClick(bangumi: Bangumi) {
        info(bangumi.name)
    }

    fun onTabSelect(position: Int) {
        currentWeekDayValue.value = position
        dayBangumis.value = timeTableBagnumis[position]
    }

    private fun errorHandle(e: Exception) {
        info(e.toString())
        error.value = e
        if (loading.value == true) loading.value = false
    }

    private fun getTodayWeekValue(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.now().dayOfWeek.value - 1
        } else {
            val todayWeek = SimpleDateFormat("E", Locale.getDefault()).format(System.currentTimeMillis())
            val weekText = BangumiApp.getContext().resources.getStringArray(R.array.week)
            weekText.forEachIndexed { index, s ->
                if (s == todayWeek) {
                    return index
                }
            }
            return 0
        }
    }
}
