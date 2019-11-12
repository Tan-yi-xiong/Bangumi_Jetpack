package com.tyxapp.bangumi_jetpack.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ListFragment
import com.tyxapp.bangumi_jetpack.main.home.adapter.CategoryAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.CategoryViewModel
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.setListener

class CategoryFragment : ListFragment() {

    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var viewModel: CategoryViewModel

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_enter_anim).apply {
                setListener {
                    mainViewModel.homeDataRepository.observe {
                        if (!::viewModel.isInitialized) {
                            viewModel = ViewModelProviders.of(
                                this@CategoryFragment,
                                InjectorUtils.provideCategoryViewModelFactory(it)
                            ).get(CategoryViewModel::class.java)
                            addObserver()
                        } else {
                            viewModel.replaceViewModel(it)
                        }
                    }
                }
            }
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            initView()
        }
    }

    private fun initView() {
        with(mRecyclerView) {
            adapter = CategoryAdapter()
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setHasFixedSize(true)
            setBackgroundColor(Color.WHITE)
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
