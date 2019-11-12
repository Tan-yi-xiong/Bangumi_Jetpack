package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.home.adapter.BangumiFollowAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.BangumiFollowViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.views.snack

class FollowBangumiFragment : ListFragment() {
    private val viewModel by viewModels<BangumiFollowViewModel> {
        InjectorUtils.provideBangumiFollowViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            showLoading()
            mRecyclerView.adapter = BangumiFollowAdapter(viewModel)
            addObserver()
        }
    }

    private fun addObserver() {
        with(viewModel) {
            followBangumis.observe {
                if (it.isEmpty()) {
                    showEmpty()
                } else {
                    (mRecyclerView.adapter as BangumiFollowAdapter).submitList(it)
                    showContent()
                }
            }

            removeMessage.observe {
                mRecyclerView.snack(it)
            }
        }
    }

    companion object {
        fun newInstance() = FollowBangumiFragment()
    }
}
