package com.tyxapp.bangumi_jetpack.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.navigation.NavigationView
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.parsers.ParserFactory
import com.tyxapp.bangumi_jetpack.utilities.*
import com.tyxapp.bangumi_jetpack.views.alertBuilder
import com.tyxapp.bangumi_jetpack.views.noButton
import com.tyxapp.bangumi_jetpack.views.yesButton
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.browse
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast


// 通知栏intent key
const val START_DOWNLOAD = "START_DOWNLOAD"
const val IMAGE_REQUEST_CODE = 10

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val mainViewModel by lazy {
        ViewModelProviders.of(this, MainViewModelFactor()).get(MainViewModel::class.java)
    }
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil.setTheme(this)
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


        //创建主页Repository供主页的Fragment使用
        setHomeDataRepository()

        //drawerLayout能否滑出navigationView
        drawerLayout = findViewById(R.id.drawerlayout)
        if (!PrefUtils.getDrawerLayoutIsUnLock()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        navigationView = findViewById(R.id.navigationView)
        with(navigationView) {
            getHeaderView(0).setOnClickListener {
                alertBuilder(R.string.text_setting_header_image, R.string.text_setting_header_image_msg) {
                    yesButton(R.string.text_setting_header_image_coustom) {
                        PermissionUtil.requestPermissions(this@MainActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)) { allDenied ->
                            if (allDenied) {
                                selectImage()
                            }
                        }
                    }

                    noButton(R.string.text_setting_header_image_local) {
                        loadLocalmage()
                        PrefUtils.setCustomHeaderImagePath(null)
                        it.cancel()
                    }
                }.show()
            }

            val hearderImagePath = PrefUtils.getCustomHeaderImagePath()
            if (hearderImagePath == null) {
                loadLocalmage()
            } else {
                loadNavigationViewHeadImage(hearderImagePath)
            }
        }


        val navGraphIds = listOf(
            R.navigation.home,
            R.navigation.history,
            R.navigation.my_download,
            R.id.menu_night_light_switch,
            R.navigation.setting
        )

        navigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.main_content
        )

        //检查更新
        if (PrefUtils.isAutoCheckUpdate()) {
            mainViewModel.checkAppUpdate()
        }
        mainViewModel.shouldShowUpdateDialog.observe(this) {
            alertBuilder(R.string.text_app_update_title, R.string.text_app_update_message) {
                yesButton(R.string.text_app_update_now) {
                    browse("https://github.com/Tan-yi-xiong/Bngumi_Jetpack/blob/master/release/app-release.apk")
                }
                noButton { it.cancel() }
            }.show()
        }
    }

    private fun loadLocalmage() {
        if (PrefUtils.isDayNight()) {
            loadNavigationViewHeadImage(R.drawable.header_img_night)
        } else {
            loadNavigationViewHeadImage(R.drawable.header_img_light)
        }
    }

    private fun selectImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    private fun loadNavigationViewHeadImage(res: Any) {
        (navigationView.getHeaderView(0) as ImageView).bindImage(res)
    }

    private fun setHomeDataRepository() {
        mainViewModel.homeDataRepository.value = when (PrefUtils.getHomeSourceName()) {
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
            BangumiSource.BimiBimi.name -> InjectorUtils.getHomeDataRepository(
                ParserFactory.createHomePageParser(
                    BangumiSource.BimiBimi
                )
            )
            else -> throw IllegalAccessException("没有此主页")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.key_home_page)) {
            setHomeDataRepository()
        } else if (key == getString(R.string.key_dl_unlock)) {
            val lockMode = if (PrefUtils.getDrawerLayoutIsUnLock()) {
                DrawerLayout.LOCK_MODE_UNLOCKED
            } else {
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            }
            drawerLayout.setDrawerLockMode(lockMode)
        }
    }

    override fun onBackPressed() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * 重启Activity, 切换主题使用
     */
    internal fun restartActivity() {
        val cyIntent = intent
        finish()
        overridePendingTransition(
            R.anim.nav_default_enter_anim,
            R.anim.nav_default_exit_anim
        )
        BangumiApp.getContext().startActivity(cyIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionUtil.showSettingPermissionsDialog(this)
                } else {
                    toast("你取消了授权")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            IMAGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val imgUrl = data!!.data!!
                        val cursor = contentResolver.query(
                            imgUrl, arrayOf(MediaStore.Images.Media.DATA),
                            null, null, null)!!
                        cursor.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                        val imgPath = cursor.getString(columnIndex)
                        cursor.close()
                        loadNavigationViewHeadImage(imgPath)
                        PrefUtils.setCustomHeaderImagePath(imgPath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
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
        
        if (navGraphId == R.id.menu_night_light_switch) {
            if (PrefUtils.isDayNight()) {
                with(menu[index]) {
                    setIcon(R.drawable.ic_day_light)
                    setTitle(R.string.title_day_light)
                }
            } else {
                with(menu[index]) {
                    setIcon(R.drawable.ic_day_night)
                    setTitle(R.string.text_day_night)
                }
            }
            return@forEachIndexed
        }

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

            if (selectItemId == R.id.menu_night_light_switch) {// 日夜模式切换
                PrefUtils.setIsDayNight(!PrefUtils.isDayNight())
                (context as MainActivity).restartActivity()
                return@setNavigationItemSelectedListener false
            }

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

fun delay(time: Long, action: () -> Unit) = MainScope().launch {
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