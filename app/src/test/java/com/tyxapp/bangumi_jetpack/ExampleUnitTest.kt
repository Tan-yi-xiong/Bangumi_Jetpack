package com.tyxapp.bangumi_jetpack

import com.tyxapp.bangumi_jetpack.data.parsers.BimiBimi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.net.URLDecoder
import java.net.URLEncoder
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
            flowTest().collect {
                println(Thread.currentThread().name + it)
            }
        }
    }

    suspend fun flowTest() = flow {
        for (i in 0 .. 10) {
            delay(500)
            emit(i)
        }

    }

    @Test
    fun test() {
        URLDecoder.decode("%E6%90%9C%E7%B4%A2").also { print(it) }
    }


    @Test
    fun parserTest() {
        runBlocking {
            BimiBimi().getRecommendBangumis("2019").forEach {
                println(it.name)
            }
        }
    }
}
