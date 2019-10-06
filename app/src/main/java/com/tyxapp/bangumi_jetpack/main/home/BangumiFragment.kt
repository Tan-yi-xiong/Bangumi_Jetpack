package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.databinding.BangumiFragmentBinding
import com.tyxapp.bangumi_jetpack.main.home.adapter.*
import java.lang.IndexOutOfBoundsException


class BangumiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bind = BangumiFragmentBinding.inflate(inflater, container, false)
        val tabLayout = bind.tabLayout
        val viewPager = bind.viewpager

        viewPager.adapter = BangumiViewPagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        return bind.root
    }


}
