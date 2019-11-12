package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.UIStata
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class TimeTableViewModel(
    private var mRepository: HomeDataRepository
) : ViewModel() {

    private lateinit var timeTableBagnumis: List<List<Bangumi>>
    val currentWeekDayValue = MutableLiveData<Int>()

    val uiStata = MutableLiveData<UIStata>()

    val dayBangumis: MutableLiveData<List<Bangumi>> by lazy {
        MutableLiveData<List<Bangumi>>().apply {
            getTimeTableBagnumis()
        }
    }

    private fun getTimeTableBagnumis() = viewModelScope.launch {
        try {
            uiStata.value = UIStata.LOADING
            timeTableBagnumis = mRepository.getBangumiTimeTable()
            if (currentWeekDayValue.value == null) {
                val today = getTodayWeekValue()
                currentWeekDayValue.value = today
                dayBangumis.value = timeTableBagnumis[today]
            } else {
                currentWeekDayValue.value = currentWeekDayValue.value
                dayBangumis.value = timeTableBagnumis[currentWeekDayValue.value!!]
            }
            uiStata.value = UIStata.SUCCESS
        } catch (e: Exception) {
            errorHandle(e)
        }
    }

    fun replaceRepository(repository: HomeDataRepository) {
        mRepository = repository
        getTimeTableBagnumis()
    }

    fun refresh() {
        viewModelScope.launch {
            getTimeTableBagnumis()
        }
    }

    fun onTabSelect(position: Int) {
        currentWeekDayValue.value = position
        if (position < timeTableBagnumis.size) {
            dayBangumis.value = timeTableBagnumis[position]
        }
    }

    private fun errorHandle(e: Exception) {
        LOGI(e.toString())
        uiStata.value = UIStata.error(e.toString())
    }

    /**
     * 获取今天是今个星期的第几天
     *
     */
    private fun getTodayWeekValue(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.now().dayOfWeek.value - 1
        } else {
            val todayWeek =
                SimpleDateFormat("E", Locale.getDefault()).format(System.currentTimeMillis())
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
