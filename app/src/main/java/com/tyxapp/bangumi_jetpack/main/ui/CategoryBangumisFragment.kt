package com.tyxapp.bangumi_jetpack.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.NetWordState
import com.tyxapp.bangumi_jetpack.data.State
import com.tyxapp.bangumi_jetpack.databinding.CategoryBangumisFragmentBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.adapter.CategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.observe
import com.tyxapp.bangumi_jetpack.main.viewmodels.CategoryBangumisViewModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.doOnEnd
import com.tyxapp.bangumi_jetpack.utilities.info
import com.tyxapp.bangumi_jetpack.utilities.navigateToSearchFragment

class CategoryBangumisFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: CategoryBangumisViewModel by viewModels {
        InjectorUtils.provideCategoryBangumisViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }

    private lateinit var binding: CategoryBangumisFragmentBinding
    private lateinit var mRecyclerView: RecyclerView

    private lateinit var categoryArg: String


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            return AnimationUtils.loadAnimation(requireContext(), nextAnim).apply {
                doOnEnd {
                    addObserver()
                }
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initView(inflater, container)
        return binding.root
    }

    private fun initView(inflater: LayoutInflater, container: ViewGroup?) {
        categoryArg = arguments?.getString(ARG_KEY)
            ?: throw NullPointerException("CategoryBangumisFragment 必须传递参数")

        binding = CategoryBangumisFragmentBinding.inflate(inflater, container, false)
        mRecyclerView = binding.recyclerView

        binding.root.findViewById<View>(R.id.errorView).setOnClickListener {
            viewModel.retry()
        }

        with(mRecyclerView) {
            adapter = CategoryBanumiAdapter()
            setHasFixedSize(true)
        }

        with(binding.toolBar) {
            title = categoryArg

            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.search ->  {
                        (requireActivity() as MainActivity).navigateToSearchFragment(this@CategoryBangumisFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun addObserver() = with(viewModel) {
        categoryBangumis.observe(this@CategoryBangumisFragment) {
            (mRecyclerView.adapter as CategoryBanumiAdapter).submitList(it)
        }

        netWordState.observe(this@CategoryBangumisFragment) {
            when (it.state) {
                State.SUCCESS -> showContent()
                State.LOADING -> showLoading()
                State.ERROR -> {
                    info(it.msg)
                    showError()
                }
            }
        }

        getCategoryBangumis(categoryArg)
    }

    private fun showLoading() {
        binding.isError = false
        binding.isLoading = true
        binding.showContent = false
    }

    private fun showError() {
        binding.isError = true
        binding.isLoading = false
        binding.showContent = false
    }

    private fun showContent() {
        binding.isError = false
        binding.isLoading = false
        binding.showContent = true
    }

    companion object {
        const val ARG_KEY = "arg_key"

        fun newInstance(word: String): CategoryBangumisFragment {
            val bundle = Bundle()
            bundle.putString(ARG_KEY, word)

            return CategoryBangumisFragment().apply {
                arguments = bundle
            }

        }
    }
}
