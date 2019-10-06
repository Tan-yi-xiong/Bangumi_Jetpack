package com.tyxapp.bangumi_jetpack

import android.app.Application
import android.content.Context

class BangumiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        private var appContext: Context? = null

        fun getContext() = appContext!!
    }
}