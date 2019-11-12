package com.tyxapp.bangumi_jetpack.main.setting

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.kk.taurus.ijkplayer.IjkPlayer
import com.kk.taurus.playerbase.config.PlayerConfig
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils
import com.tyxapp.bangumi_jetpack.utilities.formatFileSize
import kotlinx.coroutines.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.defaultSharedPreferences

class MainSettingFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val mainScope by lazy(LazyThreadSafetyMode.NONE) { MainScope() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences_setting, rootKey)
        // 主页
        val currentHomeSource = requireContext().defaultSharedPreferences
            .getString(this.getString(R.string.key_home_page), BangumiSource.DiliDili.name)
        val homeListPreference = findPreference<ListPreference>(getString(R.string.key_home_page))
        homeListPreference?.summary = currentHomeSource

        //解码
        val defDecodePlanName = resources.getStringArray(R.array.decoder_plan_name)[0]
        val decodePlanName = requireContext().defaultSharedPreferences
            .getString(this.getString(R.string.key_decoder_plan), defDecodePlanName)
        val decodePlanPreference = findPreference<ListPreference>(getString(R.string.key_decoder_plan))
        decodePlanPreference?.summary = decodePlanName

        //版本
        val version = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        val versionPreference = findPreference<Preference>("versions")
        versionPreference?.summary = version

        //缓存
        val cachePreference = findPreference<Preference>(getString(R.string.key_clear_cache))
        cachePreference?.summary = formatFileSize(getGildeCache())

        //最大下载
        val maxDownload = requireContext().defaultSharedPreferences
            .getInt(getString(R.string.key_max_download), 3)
        val maxDownloadPreference = findPreference<Preference>(getString(R.string.key_max_download))
        maxDownloadPreference?.summary = maxDownload.toString()
    }

    private fun getGildeCache(): Long {
        val files = Glide.getPhotoCacheDir(requireContext())
        var size = 0L
        if (files != null && files.isDirectory) {
            files.listFiles().forEach { size += it.length() }
        }
        return size
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when(preference?.key) {
            getString(R.string.key_clear_cache) -> {
                mainScope.launch {
                    clearCache()
                    //刷新清空后缓存大小
                    findPreference<Preference>(getString(R.string.key_clear_cache))?.summary =
                        formatFileSize(getGildeCache())
                }
                true
            }

            getString(R.string.key_max_download) -> {
                showDialog()
                true
            }

            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun showDialog() {
        val view = layoutInflater.inflate(R.layout.layout_seekbar_dialog, null, false)
        val dialog = requireActivity().alert {
            customView = view
        }.show()
        val yesButton = view.findViewById<Button>(R.id.yes_button)
        val noButton = view.findViewById<Button>(R.id.no_button)
        val seekText = view.findViewById<TextView>(R.id.seek_text)
        val seekBar = view.findViewById<SeekBar>(R.id.seekbar)
        val preference = findPreference<Preference>(getString(R.string.key_max_download))

        yesButton.setOnClickListener {
            requireContext().defaultSharedPreferences.edit(true) {
                putInt(getString(R.string.key_max_download) ,seekBar.progress)
            }
            preference?.summary = seekBar.progress.toString()
            dialog.dismiss()
        }
        noButton.setOnClickListener { dialog.dismiss() }

        seekBar.max = 5
        seekBar.progress = preference?.summary?.toString()?.toInt() ?: 3
        seekText.text = preference?.summary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.min = 1
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && progress < 1) {
                    seekBar.progress = 1
                }
                seekText.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    override fun onResume() {
        super.onResume()
        requireContext().defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.key_decoder_plan)) {
            val plan = resources.getStringArray(R.array.decoder_plan_value)
            val planPreference = findPreference<ListPreference>(getString(R.string.key_decoder_plan))
            when(sharedPreferences?.getString(key, plan[0])) {
                plan[0] -> {
                    planPreference?.summary = plan[0]
                    if (PlayerConfig.getDefaultPlanId() != IjkPlayer.PLAN_ID) {
                        if (PlayerConfig.getPlan(IjkPlayer.PLAN_ID) == null) {
                            IjkPlayer.init(BangumiApp.getContext())
                        } else {
                            PlayerConfig.setDefaultPlanId(IjkPlayer.PLAN_ID)
                        }
                    }
                }

                plan[1] -> {
                    planPreference?.summary = plan[1]
                    if (PlayerConfig.getDefaultPlanId() != PlayerConfig.DEFAULT_PLAN_ID) {
                        PlayerConfig.setDefaultPlanId(PlayerConfig.DEFAULT_PLAN_ID)
                    }
                }
            }
        } else if (key == getString(R.string.key_home_page)) {
            val sourceName = PrefUtils.getHomeSourceName()
            findPreference<Preference>(key)?.summary = sourceName
        }
    }

    private suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            Glide.get(requireContext()).clearDiskCache()
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        requireContext().defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    companion object {
        fun getInstance() = MainSettingFragment()
    }
}