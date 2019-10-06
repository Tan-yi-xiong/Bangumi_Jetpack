package com.tyxapp.bangumi_jetpack.main

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.parsers.Zzzfun
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.info

class MainActivity : AppCompatActivity() {
    private val mainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_main)

        val homeKey = PreferenceManager.getDefaultSharedPreferences(this).getString(
            getString(R.string.key_home_page),
            BangumiSource.Zzzfun.name
        )
        //创建主页解析供主页的Fragment使用
        mainViewModel.homeDataRepository.value = when (homeKey) {
            BangumiSource.Zzzfun.name -> InjectorUtils.getHomeDataRepository(Zzzfun())
            else -> throw IllegalAccessException("没有此主页")
        }
    }
}
