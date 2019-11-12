package com.tyxapp.bangumi_jetpack.main.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.BangumiFragmentBinding
import com.tyxapp.bangumi_jetpack.main.home.adapter.BangumiViewPagerAdapter
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils
import org.jetbrains.anko.defaultSharedPreferences


class BangumiFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: BangumiFragmentBinding

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val keyString = getString(R.string.key_home_page)
        if (keyString == key) {
            val sourceName = PrefUtils.getHomeSourceName()
            binding.tabLayout.getTabAt(0)?.text = sourceName
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BangumiFragmentBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewpager

        viewPager.adapter =
            BangumiViewPagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        requireActivity().defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

}
