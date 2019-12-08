package com.tyxapp.bangumi_jetpack.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.setting.adapter.ThemeSettingAdapter

class ThemeSettingFragment : ListFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mRecyclerView.adapter = ThemeSettingAdapter()
            showContent()
        }
    }

    companion object {
        fun getInstance() = ThemeSettingFragment()
    }
}