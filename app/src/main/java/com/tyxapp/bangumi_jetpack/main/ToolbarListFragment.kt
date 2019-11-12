package com.tyxapp.bangumi_jetpack.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.FragmentToolbarListBinding

abstract class ToolbarListFragment : Fragment() {
    lateinit var bind: FragmentToolbarListBinding
    lateinit var mRecyclerView: RecyclerView
    lateinit var mProgressBar: ProgressBar
    lateinit var mErrorView: View
    lateinit var mEmpttyView: View
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mToolBar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = FragmentToolbarListBinding.inflate(inflater, container, false)
        return bind.apply {
            mRecyclerView = recyclerView
            mProgressBar = progressBar
            mErrorView = root.findViewById(R.id.errorView)
            mEmpttyView = root.findViewById(R.id.emptyView)
            mSwipeRefreshLayout = swipeRefreshLayout
            mSwipeRefreshLayout.isEnabled = false
            mToolBar = toolBar
        }.root
    }

    /**
     * 等于2隐藏其他显示加载
     */
    fun showLoading() {
        bind.uiState = 2
    }

    /**
     * 等于1隐藏其他显示内容
     */
    fun showContent() {
        bind.uiState = 1
    }

    /**
     *大于5显示错误页面
     */
    fun showError() {
        bind.uiState = 6
    }

    /**
     * 少于0显示空数据
     */
    fun showEmpty() {
        bind.uiState = -1
    }

    inline fun <reified T> LiveData<T>.observe(crossinline action: (T) -> Unit) {
        observe(this@ToolbarListFragment) { action(it) }
    }

}