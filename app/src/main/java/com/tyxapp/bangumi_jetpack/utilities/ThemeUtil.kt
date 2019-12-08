package com.tyxapp.bangumi_jetpack.utilities

import android.app.Activity
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R

class ThemeUtil {
    companion object {
        private val themeNameWithRes: LinkedHashMap<String, Int> = LinkedHashMap()

        private val lightThemeRes = arrayOf(
            R.style.WhiteTheme,
            R.style.PinkTheme,
            R.style.BrowTheme,
            R.style.DeepOrangeTheme,
            R.style.DeepPurpleTheme,
            R.style.IndigoTheme,
            R.style.TealTheme,
            R.style.kuanTheme,
            R.style.zhihuTheme,
            R.style.wangyiTheme,
            R.style.biliTheme
        )

        private val nightThemeRes = arrayOf(
            R.style.DarkTheme_White,
            R.style.DarkTheme_Pink,
            R.style.DarkTheme_Brow,
            R.style.DarkTheme_DeepOrange,
            R.style.DarkTheme_DeepPurple,
            R.style.DarkTheme_Indigo,
            R.style.DarkTheme_Teal,
            R.style.DarkTheme_kuan,
            R.style.DarkTheme_zhihu,
            R.style.DarkTheme_wangyi,
            R.style.DarkTheme_bili
        )

        init {
            val themeNames = BangumiApp.getContext().resources.getStringArray(R.array.theme_name)
            themeNames.forEachIndexed { index, name ->
                themeNameWithRes[name] = lightThemeRes[index]
            }
        }

        private fun getThemeRes(themeName: String): Int = themeNameWithRes[themeName]!!

        fun setTheme(activity: Activity) {
            val isDark: Boolean = PrefUtils.isDayNight()
            val currentTheme: String = PrefUtils.getCurrentTheme()
            val themeNames: Array<String> = BangumiApp.getContext().resources.getStringArray(R.array.theme_name)
            if (isDark) {
                themeNames.forEachIndexed { index, name ->
                    if (name == currentTheme) {
                        activity.setTheme(nightThemeRes[index])
                        return
                    }
                }
            } else {
                // 默认主题为第一个
                if (currentTheme != themeNames[0]) {
                    activity.setTheme(getThemeRes(currentTheme))
                }
            }
        }
    }
}