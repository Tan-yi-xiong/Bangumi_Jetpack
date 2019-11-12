package com.tyxapp.bangumi_jetpack

import com.tyxapp.bangumi_jetpack.data.parsers.Dilidili
import com.tyxapp.bangumi_jetpack.data.parsers.Qimi
import com.tyxapp.bangumi_jetpack.data.parsers.Silisili
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        runBlocking {
            val a1 = async(Dispatchers.IO) {
                println("fdsas")
                for (i in 1..5000) {
                    println("我是$i")
                }

            }

            val a2 = async(Dispatchers.IO) {
                println("dddddd")
                for (i in 1..5000) {
                    println("IM$i")
                }
            }
            a1.await()
            a2.await()
        }
    }

    @Test
    fun test() {
        val lock = ReentrantLock()
        thread {
            a(lock)
        }
        thread {
            a(lock)
        }
    }

    fun a(lock: Lock) {
        var i = 0
        while (i < 50000) {
            val a = "sdafasdf"
            val b = "asdfasdasdf"
            println("$i  ${Thread.currentThread().name}")
            i++
        }
    }

    @Test
    fun parserTest() {
        runBlocking {
            val dilidili = Dilidili()
            dilidili.getJiList("hpqy3")
            dilidili.getRecommendBangumis("hpqy3").forEach {
                println(it)
            }
        }
    }
}
