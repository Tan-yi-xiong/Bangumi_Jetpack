package com.tyxapp.bangumi_jetpack.main.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.State
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.home.adapter.DefaultNetWorkBangumiAdapter
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.main.home.adapter.DilidiliHomeAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.NetWorkBangumiViewModel
import com.tyxapp.bangumi_jetpack.utilities.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast

class NetWorkBangumiFragment : ListFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var viewModel: NetWorkBangumiViewModel
    private val tabLayout: TabLayout by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.parentFragment!!.view!!.findViewById<TabLayout>(R.id.tabLayout)
    }
    private val bottomNavigationView by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.parentFragment!!.parentFragment!!.view!!.findViewById<BottomNavigationView>(
            R.id.bottomNavigationView
        )
    }
    private val topLine: View by lazy(LazyThreadSafetyMode.NONE) {
        parentFragment!!.parentFragment!!.view!!.findViewById<View>(R.id.topLine)
    }


    companion object {
        fun newInstance() = NetWorkBangumiFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            initView()
            mainViewModel.homeDataRepository.observe {
                mRecyclerView.adapter = getHomeAdapter()
                if (!::viewModel.isInitialized) {
                        viewModel = ViewModelProviders.of(
                            this,
                            InjectorUtils.provideNetWorkBangumiViewModelFactory(it)
                        ).get(NetWorkBangumiViewModel::class.java)
                        addObserver()
                } else {
                    viewModel.replaceRepository(it)
                }
            }

        }
    }

    @SuppressLint("ResourceType")
    private fun initView() {
        //嵌套RecyclerView滑动会有卡顿,
        // 通过嵌套NestedScrollView一次把RecyclerView的item加载完解决(暂时只想到这样)
        val scrollView = NestedScrollView(requireContext())
        scrollView.id = -10086
        mSwipeRefreshLayout.removeViewAt(0)
        scrollView.addView(mRecyclerView)
        mSwipeRefreshLayout.addView(scrollView)



        //setSwipeRefreshLayout
        with(mSwipeRefreshLayout) {
            isEnabled = true
            setOnRefreshListener { viewModel.refresh() }
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }

        //setRecyclerView
        with(mRecyclerView) {
            setHasFixedSize(true)
        }

        mErrorView.setOnClickListener { viewModel.refresh() }
    }

    private fun getHomeAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>? {

        return when (PrefUtils.getHomeSourceName()) {
            BangumiSource.Zzzfun.name -> DefaultNetWorkBangumiAdapter()
            BangumiSource.DiliDili.name -> DilidiliHomeAdapter(requireActivity() as AppCompatActivity)
            else -> throw IllegalAccessException("没有此资源主页")
        }
    }

    private fun addObserver() {

        viewModel.bangumis.observe {
            (mRecyclerView.adapter as DefaultNetWorkBangumiAdapter).submitHomeData(it)
        }

        viewModel.uiState.observe {
            mSwipeRefreshLayout.isRefreshing = it.isRefreshing

            when (it.netWordState.state) {
                State.SUCCESS -> {
                    if (!mRecyclerView.isVisible) {
                        bottomNavigationView.isGone(false)
                        tabLayout.isGone(false)
                        topLine.isGone(false)
                    }
                    showContent()
                }

                State.ERROR -> {
                    if (mRecyclerView.adapter?.itemCount == 0) {
                        showError()
                    } else {
                        requireActivity().toast(getString(R.string.text_refresh_error))
                    }
                }

                State.LOADING -> {
                    if (!it.isRefreshing) {
                        showLoading()
                    }
                }
            }
        }
    }
}

