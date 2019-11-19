package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ToolbarListFragment
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.DiliBangumi
import com.tyxapp.bangumi_jetpack.data.NetWordState
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.main.home.adapter.CategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.home.adapter.DiliCategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.CategoryBangumisViewModel
import com.tyxapp.bangumi_jetpack.utilities.*

class CategoryBangumisFragment : ToolbarListFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: CategoryBangumisViewModel by viewModels {
        InjectorUtils.provideCategoryBangumisViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }

    private lateinit var categoryArg: String


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            return AnimationUtils.loadAnimation(requireContext(), nextAnim).apply {
                setListener {
                    addObserver()
                }
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack(
                HOME_TO_CB_STASK_NAME,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }


    /**
     * 不能滑出侧栏
     *
     */
    override fun canSkidDrawerLayout(): Boolean {
        return false
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
        categoryArg = arguments?.getString(ARG_KEY)
            ?: throw NullPointerException("CategoryBangumisFragment 必须传递参数")

        mErrorView.setOnClickListener { viewModel.retry() }

        with(mRecyclerView) {
            val sourceName = PrefUtils.getHomeSourceName()

            adapter = if (sourceName == BangumiSource.DiliDili.name) {
                DiliCategoryBanumiAdapter()
            } else {
                CategoryBanumiAdapter()
            }
            setHasFixedSize(true)
        }

        with(mToolBar) {
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

    @Suppress("UNCHECKED_CAST")
    private fun addObserver() = with(viewModel) {
        categoryBangumis.observe(this@CategoryBangumisFragment) {
            val adapter = mRecyclerView.adapter
            if (adapter is CategoryBanumiAdapter) {
                adapter.submitList(it)
            } else {
                (adapter as DiliCategoryBanumiAdapter).submitList(it as List<DiliBangumi>)
            }
        }

        initialLoad.observe(this@CategoryBangumisFragment) {
            when (it.netWordState) {
                NetWordState.LOADING -> showLoading()

                NetWordState.SUCCESS -> {
                    if (!mRecyclerView.isVisible) { //执行进场动画
                        mRecyclerView.translationFadeIn()
                    }
                    showContent()
                }
                else -> {
                    LOGI(it.netWordState.msg)
                    showError()
                }
            }
        }

        getCategoryBangumis(categoryArg)
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
