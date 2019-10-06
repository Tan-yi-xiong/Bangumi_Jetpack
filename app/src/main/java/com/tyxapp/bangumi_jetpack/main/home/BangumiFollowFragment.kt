package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import com.tyxapp.bangumi_jetpack.main.ListFragment

class BangumiFollowFragment : ListFragment() {

    companion object {
        fun newInstance() = BangumiFollowFragment()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showError()
    }

}
