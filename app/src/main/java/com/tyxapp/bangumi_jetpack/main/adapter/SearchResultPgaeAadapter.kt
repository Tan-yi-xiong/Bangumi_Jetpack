package com.tyxapp.bangumi_jetpack.main.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.main.ui.SearchResultChildFragment

class SearchResultPgaeAadapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    private val tabWithFragments = LinkedHashMap<BangumiSource, Fragment>().apply {
        BangumiSource.values().forEach {
            this[it] = SearchResultChildFragment.newInstance(it.name)
        }
    }

    override fun getItemCount(): Int = BangumiSource.values().size

    override fun createFragment(position: Int): Fragment {
        val bangumiSource = BangumiSource.values()[position]
        return tabWithFragments[bangumiSource] ?: throw IndexOutOfBoundsException("没有该标签Fragment")
    }
}