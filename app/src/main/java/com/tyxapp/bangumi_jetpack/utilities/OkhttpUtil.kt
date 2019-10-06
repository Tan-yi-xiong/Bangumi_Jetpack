package com.tyxapp.bangumi_jetpack.utilities

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.NullPointerException

enum class RequestMode {
    GET, POST
}

fun info(msg: String) = Log.i("MainActivity", msg)

object OkhttpUtil {
    private val client by lazy { OkHttpClient() }

    fun getResponseData(
            url: String,
            requestMode: RequestMode = RequestMode.GET,
            requestBody: RequestBody? = null
    ): String {
        val request: Request = when (requestMode) {
            RequestMode.GET -> Request.Builder().url(url).build()

            RequestMode.POST -> {
                requestBody ?: throw NullPointerException("POST请求时请求体不能为空")
                Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()
            }
        }
        val responseBody = client.newCall(request).execute().body
                ?: throw NullPointerException("应答体为空")
        return responseBody.string()
    }
}

