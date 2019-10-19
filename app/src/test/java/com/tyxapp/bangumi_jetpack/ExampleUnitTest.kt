package com.tyxapp.bangumi_jetpack

import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.parsers.Silisili
import com.tyxapp.bangumi_jetpack.data.parsers.Zzzfun
import com.tyxapp.bangumi_jetpack.utilities.info
import org.junit.Test

import org.junit.Assert.*
import java.net.URLEncoder
import kotlin.concurrent.thread

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testParser() {
        val l = Silisili().getCategorItems()
        l.forEach {
            println("${it.categorName}  ${it.cover}")
        }
    }

    @Test
    fun test() {
        val listing = Zzzfun().getSearchResult("")
        listing.liveDataPagelist.value?.forEach {
            print("${it.name}   ${it.id}")
        }
    }
}
