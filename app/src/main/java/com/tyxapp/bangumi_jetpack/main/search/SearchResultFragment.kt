package com.tyxapp.bangumi_jetpack.main.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.BangumiSourceNameConversion
import com.tyxapp.bangumi_jetpack.databinding.SearchResultFragmentBinding
import com.tyxapp.bangumi_jetpack.main.search.adapter.SearchResultPgaeAadapter
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils

class SearchResultFragment : Fragment() {

    companion object {
        fun newInstance() = SearchResultFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = SearchResultFragmentBinding.inflate(inflater, container, false)
        val viewPager2 = binding.viewPager2
        val tabLayout = binding.tabLayout

        //反射去除viewPager2到顶水波纹效果
        try {
            val field = viewPager2::class.java.getDeclaredField("mRecyclerView")
            field.isAccessible = true
            val recyclerView = field.get(viewPager2) as RecyclerView
            recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        } catch (e: Exception) {
            e.printStackTrace()
        }


        viewPager2.adapter =
            SearchResultPgaeAadapter(this)

        viewPager2.setCurrentItem(getPrioritizedSearchSourchPosistion(), false)

        TabLayoutMediator(tabLayout, viewPager2) {tab, index ->
            tab.text = BangumiSourceNameConversion.getConversionName(BangumiSource.values()[index])
        }.attach()

        return binding.root
    }

    /**
     * 优先显示的搜索结果
     *
     */
    private fun getPrioritizedSearchSourchPosistion(): Int {
        val prioritizedName = PrefUtils.getPrioritizedSearchSourch()
        BangumiSource.values().forEachIndexed { index, bangumiSource ->
            if (BangumiSourceNameConversion.getConversionName(bangumiSource) == prioritizedName) {
                return index
            }
        }
        return 0
    }
}
