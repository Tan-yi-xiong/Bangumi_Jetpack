package com.tyxapp.bangumi_jetpack.main

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.parsers.Dilidili
import com.tyxapp.bangumi_jetpack.data.parsers.ParserFactory
import com.tyxapp.bangumi_jetpack.data.parsers.Zzzfun
import com.tyxapp.bangumi_jetpack.main.mydownload.MyDownloadFragment
import com.tyxapp.bangumi_jetpack.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences

// 通知栏intent key
const val START_DOWNLOAD = "START_DOWNLOAD"

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val mainViewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_main)

        // 通过通知栏进入下载fragment
        if (intent.getBooleanExtra(START_DOWNLOAD, false)) {
            supportFragmentManager.commit {
                val navHostFragment = NavHostFragment.create(R.navigation.my_download)
                replace(R.id.main_content, navHostFragment)
            }
            return
        }

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val homeKey = PreferenceManager.getDefaultSharedPreferences(this).getString(
            getString(R.string.key_home_page),
            BangumiSource.DiliDili.name
        )
        //创建主页Repository供主页的Fragment使用
        mainViewModel.homeDataRepository.value = when (homeKey) {
            BangumiSource.Zzzfun.name -> InjectorUtils.getHomeDataRepository(
                ParserFactory.createHomePageParser(
                    BangumiSource.Zzzfun
                )
            )
            BangumiSource.DiliDili.name -> InjectorUtils.getHomeDataRepository(
                ParserFactory.createHomePageParser(
                    BangumiSource.DiliDili
                )
            )
            else -> throw IllegalAccessException("没有此主页")
        }

        //禁止侧边划出
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        //navigationView设置
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        //加载头部图片
        Glide.with(this).load(R.drawable.header_image)
            .into(navigationView.getHeaderView(0) as ImageView)

        val navGraphIds = listOf(
            R.navigation.home,
            R.navigation.history,
            R.navigation.my_download,
            R.navigation.setting
        )

        navigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.main_content
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val keyString = getString(R.string.key_home_page)
        if (key == keyString) {
            when(PrefUtils.getHomeSourceName()) {
                BangumiSource.Zzzfun.name -> {
                    mainViewModel.homeDataRepository.value = InjectorUtils.getHomeDataRepository(Zzzfun())
                }

                BangumiSource.DiliDili.name -> {
                    mainViewModel.homeDataRepository.value = InjectorUtils.getHomeDataRepository(Dilidili())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

//NavigationView处理返回栈栈名
const val NAVIGATION_VIEW_STACK_NAME = "NAVIGATION_VIEW_STACK_NAME"

private fun NavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int
) {
    var currentGraphId = 0
    val graphIdWithNavGraphId = LinkedHashMap<Int, Int>()
    val fristFragmentTag = getFragmentTag(0)

    navGraphIds.forEachIndexed { index, navGraphId ->

        val graphId = this.menu[index].itemId

        graphIdWithNavGraphId[graphId] = navGraphId

        if (getSelectItem().itemId == graphId) {
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                getFragmentTag(index),
                navGraphId,
                containerId
            )
            attachNavHostFragment(fragmentManager, navHostFragment, true)
            currentGraphId = graphId
        }
    }

    val fristGraphId = graphIdWithNavGraphId.keys.elementAt(0)
    var isFristFragment = fristGraphId == currentGraphId

    setNavigationItemSelectedListener { menuItem ->
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val selectItemId = menuItem.itemId
            if (selectItemId != currentGraphId) {
                delay(300) {
                    fragmentManager.popBackStack(
                        NAVIGATION_VIEW_STACK_NAME,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    if (selectItemId != fristGraphId) {

                        val navHostFragment =
                            NavHostFragment.create(graphIdWithNavGraphId[selectItemId]!!)

                        fragmentManager.commit {
                            setCustomAnimations(
                                R.anim.nav_default_enter_anim,
                                R.anim.nav_default_exit_anim,
                                R.anim.nav_default_pop_enter_anim,
                                R.anim.nav_default_pop_exit_anim
                            )
                            hide(fragmentManager.findFragmentByTag(fristFragmentTag)!!)
                            add(containerId, navHostFragment)
                            setPrimaryNavigationFragment(navHostFragment)
                            addToBackStack(NAVIGATION_VIEW_STACK_NAME)
                            setReorderingAllowed(true)
                        }
                    }
                    currentGraphId = selectItemId
                    isFristFragment = selectItemId == fristGraphId
                }
            }
            this.setCheckedItem(menuItem)
            (parent as DrawerLayout).closeDrawer(Gravity.LEFT)
            true
        }
    }

    fragmentManager.addOnBackStackChangedListener {
        if (!isFristFragment && !fragmentManager.isOnBackStask(NAVIGATION_VIEW_STACK_NAME)) {
            this.setCheckedItem(fristGraphId)
            currentGraphId = fristGraphId
            isFristFragment = true
        }
    }


}

fun delay(time: Long, action: () -> Unit) = CoroutineScope(Dispatchers.Main).launch {
    delay(time)
    action()
}

fun NavigationView.getSelectItem(): MenuItem {
    this.menu.forEach {
        if (it.isChecked) {
            return it
        }
    }

    throw IllegalAccessException("noItemSelect!")
}