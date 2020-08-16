package com.tyxapp.bangumi_jetpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.test.runner.AndroidJUnit4
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.parsers.*
import com.tyxapp.bangumi_jetpack.repository.MainRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.resume

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleInstrumentedTest {

    @Test
    fun homeData() {
        runBlocking {
            val bangumiDetail = Malimali().run {
                getRecommendBangumis("12525").forEach {
                    LOGI(it.cover)
                }
            }
//            LOGI(bangumiDetail.toString())
        }

    }
}
