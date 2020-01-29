package com.tyxapp.bangumi_jetpack

import com.tyxapp.bangumi_jetpack.data.parsers.BimiBimi
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
            print(URLDecoder.decode("%25E6%2588%2591%25E7%259A%2584"))
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
            BimiBimi().getRecommendBangumis("2019").forEach {
                println(it.name)
            }
        }
    }
}
