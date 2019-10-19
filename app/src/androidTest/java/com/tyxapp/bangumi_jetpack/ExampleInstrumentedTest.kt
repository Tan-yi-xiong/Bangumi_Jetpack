package com.tyxapp.bangumi_jetpack

import androidx.test.runner.AndroidJUnit4
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.parsers.Silisili
import com.tyxapp.bangumi_jetpack.utilities.info
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.concurrent.thread

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun dBTest() {
        val searchDao = AppDataBase.getInstance().searchDao()
        searchDao.getSearchWords().forEach {
            println(it.word)
        }
    }
}
