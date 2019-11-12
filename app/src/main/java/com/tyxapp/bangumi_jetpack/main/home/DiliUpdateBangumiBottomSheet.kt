package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.NetWordState
import com.tyxapp.bangumi_jetpack.databinding.LayoutDiliUpdateBottomSheetBinding
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.main.home.adapter.DiliBottomSheetAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.DilidiliUpdateBangumiViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils

class DiliUpdateBangumiBottomSheet : BottomSheetDialogFragment() {

    private lateinit var bind: LayoutDiliUpdateBottomSheetBinding
    private lateinit var mRecyclerView: RecyclerView
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: DilidiliUpdateBangumiViewModel by viewModels {
        InjectorUtils.provideDilidiliUpdateBangumiViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = LayoutDiliUpdateBottomSheetBinding.inflate(inflater, container, false)
        mRecyclerView = bind.recyclerView
        mRecyclerView.adapter = DiliBottomSheetAdapter()
        mRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        bind.close.setOnClickListener { dismiss() }
        bind.root.findViewById<View>(R.id.errorView).setOnClickListener { viewModel.retry() }
        showLoading()
        addObserver()
        return bind.root
    }

    private fun addObserver() {
        viewModel.bangumisLiveData.observe(this) {
            (mRecyclerView.adapter as DiliBottomSheetAdapter).submitList(it)
        }

        viewModel.initialState.observe(this) {
            when(it.netWordState) {
                NetWordState.SUCCESS -> showContent()
                NetWordState.LOADING -> showLoading()
                else -> showError()
            }
        }

        viewModel.loadData()
    }

    private fun showContent() {
        bind.viewState = 2
    }

    private fun showError() {
        bind.viewState = -1
    }

    private fun showLoading() {
        bind.viewState = 1
    }

    companion object{
        fun getInstance() = DiliUpdateBangumiBottomSheet()
    }
}