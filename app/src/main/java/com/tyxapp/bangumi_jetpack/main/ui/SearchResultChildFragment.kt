package com.tyxapp.bangumi_jetpack.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.paging.PagedList
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.NetWordState
import com.tyxapp.bangumi_jetpack.data.State
import com.tyxapp.bangumi_jetpack.data.parsers.IsearchParse
import com.tyxapp.bangumi_jetpack.data.parsers.Zzzfun
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.adapter.SearchResultChildAdapter
import com.tyxapp.bangumi_jetpack.main.viewmodels.SearchResultChildViewModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.SearchViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.info

/**
 * 搜索结果列表
 *
 */

class SearchResultChildFragment : ListFragment() {

    private val parentViewModel: SearchViewModel by viewModels(ownerProducer = { parentFragment!!.parentFragment!! })
    private val viewModel: SearchResultChildViewModel by viewModels {
        InjectorUtils.provideSearchResultChildRepository(getSearchParser())
    }

    private lateinit var adapter: SearchResultChildAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            initView()
            addObserver()
        }
    }

    private fun addObserver() {

        parentViewModel.searchWord.observe { viewModel.setSearchWord(it) }

        //结果集
        viewModel.searchResults.observe {
            adapter.submitList(it)
        }

        //加载下一页状态
        viewModel.netWordState.observe {
            adapter.submitNetWordState(it)
        }

        //初次加载
        viewModel.initialLoad.observe { initialLoad ->
            if (initialLoad.netWordState == NetWordState.LOADING) {
                showLoading()
            } else if (initialLoad.netWordState.state == State.ERROR) {
                showError()
            } else {
                if (initialLoad.isDataEmpty) {
                    showEmpty()
                } else {
                    showContent()
                }
            }
        }
    }

    private fun initView() {
        adapter = SearchResultChildAdapter(viewModel.retry())
        mRecyclerView.adapter = adapter

        mErrorView.setOnClickListener { viewModel.retry().invoke() }
    }

    private fun getSearchParser(): IsearchParse {
        val bangumiSource = arguments?.getString(SRCF_ARG_KEY)
            ?: throw NullPointerException("SearchResultChildFragment创建必须传递参数")

        return when (bangumiSource) {
            BangumiSource.Zzzfun.name -> Zzzfun()
            else -> Zzzfun()
        }
    }

    companion object {
        private const val SRCF_ARG_KEY = "SRCF_ARG_KEY"

        fun newInstance(bangumiSource: String): SearchResultChildFragment {
            return SearchResultChildFragment().apply {
                this.arguments = Bundle().apply {
                    putString(SRCF_ARG_KEY, bangumiSource)
                }
            }
        }
    }
}