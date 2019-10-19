package com.tyxapp.bangumi_jetpack.main.adapter

import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.main.ui.BangumiFollowFragment
import com.tyxapp.bangumi_jetpack.main.ui.NetWorkBangumiFragment

const val NETWORK_BANGUMI = 0
const val MY_FOLLOW_BANGUMI = 1


class BangumiViewPagerAdapter(
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabWithFragments = mapOf(
            NETWORK_BANGUMI to { NetWorkBangumiFragment.newInstance() },
            MY_FOLLOW_BANGUMI to { BangumiFollowFragment.newInstance() }
    )

    override fun getItem(position: Int): Fragment {
        return tabWithFragments[position]?.invoke() ?: throw IndexOutOfBoundsException("没有更多fragment")
    }

    override fun getCount(): Int = tabWithFragments.size

    override fun getPageTitle(position: Int): CharSequence? {
        return getTabText(position)
    }

    private fun getTabText(position: Int): CharSequence? = when (position) {
        NETWORK_BANGUMI -> PreferenceManager.getDefaultSharedPreferences(BangumiApp.getContext()).getString(
            BangumiApp.getContext().getString(R.string.key_home_page),
            BangumiSource.Zzzfun.name
        )
        MY_FOLLOW_BANGUMI -> BangumiApp.getContext().getString(R.string.favorite_bangumi)
        else -> throw java.lang.IndexOutOfBoundsException("没有更多标签文本")
    }

}