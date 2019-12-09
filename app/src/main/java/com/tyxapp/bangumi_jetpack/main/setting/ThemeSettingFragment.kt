package com.tyxapp.bangumi_jetpack.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.setting.adapter.ThemeSettingAdapter

class ThemeSettingFragment : ListFragment() {

    private lateinit var toolbar: Toolbar
    private var toolbarTitle: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolbar = parentFragment!!.view!!.findViewById(R.id.toolBar)
        toolbarTitle = toolbar.title.toString()
        toolbar.title = "主题风格"
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mRecyclerView.adapter = ThemeSettingAdapter()
            showContent()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbar.title = toolbarTitle
    }

    companion object {
        fun getInstance() = ThemeSettingFragment()
    }
}