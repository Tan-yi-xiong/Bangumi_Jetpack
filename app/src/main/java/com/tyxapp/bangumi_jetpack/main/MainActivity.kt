package com.tyxapp.bangumi_jetpack.main

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.parsers.Zzzfun
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.utilities.*

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
        //创建主页Repository供主页的Fragment使用
        mainViewModel.homeDataRepository.value = when (homeKey) {
            BangumiSource.Zzzfun.name -> InjectorUtils.getHomeDataRepository(Zzzfun())
            else -> throw IllegalAccessException("没有此主页")
        }

        //禁止侧边划出
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        //navigationView设置
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val navGraphIds = listOf(
            R.navigation.home,
            R.navigation.history,
            R.navigation.my_download
        )

        navigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.main_content
        )
    }

    override fun onBackPressed() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        for (i in backStackEntryCount - 1 downTo 0) {
            //如果存在下面的栈, 优先按顺序处理
            when (supportFragmentManager.getBackStackEntryAt(i).name) {
                SEARCHFRAGMENT_STACK_NAME -> {
                    return super.onBackPressed()
                }

                HOME_TO_CB_STASK_NAME -> {
                    supportFragmentManager.popBackStack(
                        HOME_TO_CB_STASK_NAME,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }

                NAVIGATION_VIEW_STACK_NAME -> {
                    supportFragmentManager.popBackStack(
                        NAVIGATION_VIEW_STACK_NAME,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    return
                }
            }
        }
        super.onBackPressed()
    }
}

//NavigationView处理返回栈栈名
private const val NAVIGATION_VIEW_STACK_NAME = "NAVIGATION_VIEW_STACK_NAME"

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

fun NavigationView.getSelectItem(): MenuItem {
    this.menu.forEach {
        if (it.isChecked) {
            return it
        }
    }

    throw IllegalAccessException("noItemSelect!")
}