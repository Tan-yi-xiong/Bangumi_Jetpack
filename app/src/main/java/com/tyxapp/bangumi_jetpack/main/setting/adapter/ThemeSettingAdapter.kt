package com.tyxapp.bangumi_jetpack.main.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.ThemeSettingItemBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils
import com.tyxapp.bangumi_jetpack.utilities.isGone

class ThemeSettingAdapter : RecyclerView.Adapter<ThemeSettingAdapter.ViewHolder>() {
    private val themeNames = BangumiApp.getContext().resources.getStringArray(R.array.theme_name)
    private val themeColorPrimarys = intArrayOf(
        android.R.color.tertiary_text_dark,
        R.color.PinkThemeColor,
        R.color.BrowThemeColor,
        R.color.DeepOrange,
        R.color.DeepPurple,
        R.color.indigo,
        R.color.Teal,
        R.color.kuanTheme,
        R.color.zhihuTheme,
        R.color.wangyiTheme,
        R.color.biliTheme
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.theme_setting_item, parent, false
            )
        )
    }

    override fun getItemCount(): Int = themeNames.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.bind) {
            val themeColorPrimary = ContextCompat.getColor(BangumiApp.getContext(), themeColorPrimarys[position])
            colorIndicator.indicatorColor = themeColorPrimary

            colorText.text = themeNames[position]
            colorText.setTextColor(themeColorPrimary)

            val currentTheme = PrefUtils.getCurrentTheme()
            check.isGone = themeNames[position] != currentTheme
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ViewHolder(val bind: ThemeSettingItemBinding) : RecyclerView.ViewHolder(bind.root) {
        init {
            bind.setOnClick { view ->
                bind.colorText.text?.let {
                    PrefUtils.setCurrentTheme(it.toString())
                    (view.context as MainActivity).restartActivity()
                }
            }
        }
    }
}