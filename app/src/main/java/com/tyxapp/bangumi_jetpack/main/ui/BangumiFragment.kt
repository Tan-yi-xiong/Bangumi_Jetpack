package com.tyxapp.bangumi_jetpack.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tyxapp.bangumi_jetpack.databinding.BangumiFragmentBinding
import com.tyxapp.bangumi_jetpack.main.adapter.BangumiViewPagerAdapter


class BangumiFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bind = BangumiFragmentBinding.inflate(inflater, container, false)
        val tabLayout = bind.tabLayout
        val viewPager = bind.viewpager

        viewPager.adapter =
            BangumiViewPagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        return bind.root
    }


}
