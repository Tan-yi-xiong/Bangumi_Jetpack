package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.CategoryDetailFragmentBinding
import com.tyxapp.bangumi_jetpack.main.home.adapter.CategoryDetailAdapter
import com.tyxapp.bangumi_jetpack.main.viewmodels.CategoryBangumisModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils

class CategoryBangumisFragment : Fragment() {

    private val categoryArgs by navArgs<CategoryBangumisFragmentArgs>()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: CategoryBangumisModel by viewModels {
        InjectorUtils.provideCategoryBangumisViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }

    private lateinit var binding: CategoryDetailFragmentBinding
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CategoryDetailFragmentBinding.inflate(inflater, container, false)
        mRecyclerView = binding.recyclerView
        val toolbar = binding.toolBar
        mRecyclerView.adapter = CategoryDetailAdapter()
        toolbar.title = categoryArgs.categoryWord

        toolbar.setNavigationOnClickListener { requireActivity().findNavController(R.id.main_content).navigateUp() }
        addObserver()
        return binding.root
    }

    private fun addObserver() = with(viewModel) {
        binding.isLoading = true
        getCategoryBangumis(categoryArgs.categoryWord)
        categoryBangumis.observe(this@CategoryBangumisFragment, Observer<PagedList<Bangumi>> {
            (mRecyclerView.adapter as CategoryDetailAdapter).submitList(it)
            binding.isLoading = false
        })
    }


}
