package com.tyxapp.bangumi_jetpack.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.FragmentToolbarListBinding
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils

abstract class ToolbarListFragment : Fragment() {
    lateinit var bind: FragmentToolbarListBinding
    lateinit var mRecyclerView: RecyclerView
    lateinit var mProgressBar: ProgressBar
    lateinit var mErrorView: View
    lateinit var mEmpttyView: View
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mToolBar: Toolbar
    private  var canSkidDrawerLayout: Boolean = true
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = FragmentToolbarListBinding.inflate(inflater, container, false)
        drawerLayout = requireActivity().findViewById(R.id.drawerlayout)

        canSkidDrawerLayout = canSkidDrawerLayout()
        if (!canSkidDrawerLayout) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

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
     * 是否允许滑出[MainActivity]的DrawerLayout
     *
     */
    open fun canSkidDrawerLayout(): Boolean {
        return true
    }

    /**
     * 再次可见时重新设置能否滑动, 防止其他的Fragment修改
     *
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && !canSkidDrawerLayout) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
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

    override fun onDestroyView() {
        if (PrefUtils.getDrawerLayoutIsUnLock()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
        super.onDestroyView()
    }

}