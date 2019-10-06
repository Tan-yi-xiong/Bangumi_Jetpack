package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.home.adapter.CategoryAdapter
import com.tyxapp.bangumi_jetpack.main.viewmodels.CategoryViewModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils

class CategoryFragment : ListFragment() {

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val viewModel: CategoryViewModel by viewModels {
        InjectorUtils.provideCategoryViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            initView()
            addObserver()
        }
    }

    private fun initView() {
        with(mRecyclerView) {
            adapter = CategoryAdapter()
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setHasFixedSize(true)

        }
        mErrorView.setOnClickListener { viewModel.refresh() }
    }

    private fun addObserver() = with(viewModel) {
        categorys.observe {
            (mRecyclerView.adapter as CategoryAdapter).submitList(it)
            showContent()
        }


        error.observe {
            showError()
        }

        loading.observe { isLoading ->
            if (isLoading) showLoading()
        }

        empty.observe {
            showEmpty()
        }

    }
}
