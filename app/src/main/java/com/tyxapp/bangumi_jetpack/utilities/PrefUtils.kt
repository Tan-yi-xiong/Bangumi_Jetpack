package com.tyxapp.bangumi_jetpack.utilities

import android.annotation.SuppressLint
import androidx.core.content.edit
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import org.jetbrains.anko.defaultSharedPreferences

@SuppressLint("StaticFieldLeak")
class PrefUtils {
    companion object {
        private val context = BangumiApp.getContext()
        fun getHomeSourceName(): String? {
            return context.defaultSharedPreferences.getString(
                context.getString(R.string.key_home_page),
                BangumiSource.DiliDili.name
            )
        }

        fun getDecodePlanName(defaultvalue: String): String? {
            return context.defaultSharedPreferences.getString(
                context.getString(R.string.key_decoder_plan),
                defaultvalue
            )
        }

        /**
         * [MainActivity]侧栏是否能滑动
         *
         */
        fun getDrawerLayoutIsUnLock(): Boolean {
            return context.defaultSharedPreferences.getBoolean(
                context.getString(R.string.key_dl_unlock),
                false
            )
        }

        /**
         * 是否设置了打开应用自动检查更新
         *
         */
        fun isAutoCheckUpdate(): Boolean {
            return context.defaultSharedPreferences.getBoolean(
                context.getString(R.string.key_auto_check_updata),
                false
            )
        }

        fun getPrioritizedSearchSourch(): String {
            return context.defaultSharedPreferences.getString(
                context.getString(R.string.key_prioritized_search_sourch),
                BangumiSource.Nico.name
            )!!
        }

        fun isDayNight(): Boolean {
            return context.defaultSharedPreferences.getBoolean(
                context.getString(R.string.key_isDayNight),
                false
            )
        }

        fun setIsDayNight(isDayNight: Boolean) {
            context.defaultSharedPreferences.edit {
                putBoolean(context.getString(R.string.key_isDayNight), isDayNight)
            }
        }

        /**
         * 记录用户设置的主题
         */
        fun setCurrentTheme(themeName: String) {
            context.defaultSharedPreferences.edit { putString(context.getString(R.string.key_theme), themeName) }
        }

        fun getCurrentTheme(): String {
            return context.defaultSharedPreferences.getString(
                context.getString(R.string.key_theme),
                context.resources.getStringArray(R.array.theme_name)[0]
            )!!
        }
    }

}