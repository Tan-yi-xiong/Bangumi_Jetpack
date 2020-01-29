package com.tyxapp.bangumi_jetpack.main.search.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.BangumiSourceNameConversion
import com.tyxapp.bangumi_jetpack.main.search.SearchResultChildFragment
import com.tyxapp.bangumi_jetpack.utilities.LOGI

class SearchResultPgaeAadapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private val sourchNames = fragment.resources.getStringArray(R.array.search_source)

    private val tabWithFragments = LinkedHashMap<BangumiSource, Fragment>().apply {
        sourchNames.forEach { sourceName ->
            val bangumiSource = BangumiSourceNameConversion.nameToSource(sourceName)
            this[bangumiSource] = SearchResultChildFragment.newInstance(sourceName)
        }
    }

    override fun getItemCount(): Int = tabWithFragments.size

    override fun createFragment(position: Int): Fragment {
        val bangumiSource = BangumiSourceNameConversion.nameToSource(sourchNames[position])
        return tabWithFragments[bangumiSource] ?: throw IndexOutOfBoundsException("没有该标签Fragment")
    }
}