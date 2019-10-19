package com.tyxapp.bangumi_jetpack.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.databinding.SearchResultFragmentBinding
import com.tyxapp.bangumi_jetpack.main.adapter.SearchResultPgaeAadapter
import com.tyxapp.bangumi_jetpack.utilities.info

class SearchResultFragment : Fragment() {

    companion object {
        fun newInstance() = SearchResultFragment()
    }

    private val appBarLayout: AppBarLayout by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.view!!.findViewById<AppBarLayout>(R.id.appBarLayout)
    }
    private var prevElevation: Float = 2.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = SearchResultFragmentBinding.inflate(inflater, container, false)
        val viewPager2 = binding.viewPager2
        val tabLayout = binding.tabLayout

        prevElevation = appBarLayout.elevation

        viewPager2.adapter = SearchResultPgaeAadapter(this)

        TabLayoutMediator(tabLayout, viewPager2) {tab, index ->
            tab.text = BangumiSource.values()[index].name
        }.attach()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        appBarLayout.elevation = 0f
    }

    override fun onPause() {
        super.onPause()
        appBarLayout.elevation = prevElevation
    }

}
