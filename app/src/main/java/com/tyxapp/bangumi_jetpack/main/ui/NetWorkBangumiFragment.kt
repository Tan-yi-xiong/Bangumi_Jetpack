package com.tyxapp.bangumi_jetpack.main.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.*
import com.tyxapp.bangumi_jetpack.main.adapter.NetWorkBangumiAdapter
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.NetWorkBangumiViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.info
import com.tyxapp.bangumi_jetpack.utilities.tranFadeIn

class NetWorkBangumiFragment : ListFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val netWorkBangumiViewModel by viewModels<NetWorkBangumiViewModel> {
        InjectorUtils.provideNetWorkBangumiViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }

    companion object {
        fun newInstance() = NetWorkBangumiFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            initView()
            addObserver()
        }
    }

    @SuppressLint("ResourceType")
    private fun initView() {
        mErrorView.setOnClickListener { netWorkBangumiViewModel.refresh() }

        //嵌套RecyclerView滑动会有卡顿,
        // 通过嵌套NestedScrollView一次把RecyclerView的item加载完解决(暂时只想到这样)
        with(mRecyclerView) {
            val scrollView = NestedScrollView(requireContext())
            scrollView.id = -10086

            mSwipeRefreshLayout.removeViewAt(0)
            scrollView.addView(mRecyclerView)

            mSwipeRefreshLayout.addView(scrollView)

            //setRecyclerView
            setHasFixedSize(true)
            adapter = NetWorkBangumiAdapter()
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun addObserver() {
        netWorkBangumiViewModel.apply {
            bangumis.observe {
                (mRecyclerView.adapter as NetWorkBangumiAdapter).submitHomeData(it)
                //第一次加载才执行动画
                if (netWorkBangumiViewModel.firstLoading.value == true) {
                    mRecyclerView.tranFadeIn()
                }
                showContent()

            }

            refreshing.observe { mSwipeRefreshLayout.isRefreshing = it }

            error.observe { throwable ->
                showError()
                info(throwable.toString())
            }

            empty.observe { showEmpty() }

            firstLoading.observe {
                if (it) {
                    showLoading()
                } else {//第一次加载数据完后才初始化刷新控件, 防止第一次加载数据用户同时拉动刷新控件, 造成多次请求
                    with(mSwipeRefreshLayout) {
                        isEnabled = true
                        setOnRefreshListener { refresh() }
                        setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    }
                }
            }
        }
    }


}
