package com.tyxapp.bangumi_jetpack.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.ListFragmentBinding

abstract class ListFragment : Fragment() {
    lateinit var bind: ListFragmentBinding
    lateinit var mRecyclerView: RecyclerView
    lateinit var mProgressBar: ProgressBar
    lateinit var mErrorView: View
    lateinit var mEmpttyView: View
    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ListFragmentBinding.inflate(inflater, container, false)
        return bind.apply {
            mRecyclerView = recyclerView
            mProgressBar = progressBar
            mErrorView = root.findViewById(R.id.errorView)
            mEmpttyView = root.findViewById(R.id.emptyView)
            mSwipeRefreshLayout = swipeRefreshLayout
            mSwipeRefreshLayout.isEnabled = false
        }.root
    }

    fun showLoading() {
        bind.showContent = false
        bind.showEmpty = false
        bind.showError = false
        bind.showLoading = true
    }

    fun showContent() {
        bind.showEmpty = false
        bind.showError = false
        bind.showLoading = false
        bind.showContent = true
    } 

    fun showError() {
        bind.showEmpty = false
        bind.showLoading = false
        bind.showContent = false
        bind.showError = true
    }

    fun showEmpty() {
        bind.showLoading = false
        bind.showError = false
        bind.showContent = false
        bind.showEmpty = true
    }

    inline fun <reified T> LiveData<T>.observe(crossinline action: (T) -> Unit) {
        observe(this@ListFragment ) { action(it) }
    }
}

inline fun <reified T> LiveData<T>.observe(owner: LifecycleOwner, crossinline action: (T) -> Unit) {
    observe(owner, Observer<T> { action(it) })
}