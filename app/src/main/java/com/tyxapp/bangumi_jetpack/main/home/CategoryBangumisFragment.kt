package com.tyxapp.bangumi_jetpack.main.home

import android.content.Intent
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
import androidx.paging.PagedList
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.main.ToolbarListFragment
import com.tyxapp.bangumi_jetpack.main.home.adapter.DiliCategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.home.adapter.ICategoryAdapter
import com.tyxapp.bangumi_jetpack.main.home.adapter.ZfCategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.CategoryBangumisViewModel
import com.tyxapp.bangumi_jetpack.player.ARG_BANGUMI_SOURCE
import com.tyxapp.bangumi_jetpack.player.ARG_ID
import com.tyxapp.bangumi_jetpack.player.FOLLOW_RESULT_KEY
import com.tyxapp.bangumi_jetpack.player.PlayerActivity
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

        val homeSourcenName = PrefUtils.getHomeSourceName()!!
        mRecyclerView.adapter = CategoryBanumiAdapterFactory.getAdapter(homeSourcenName, viewModel)
        mRecyclerView.setHasFixedSize(true)

        with(mToolBar) {
            title = categoryArg

            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.search -> {
                        (requireActivity() as MainActivity).navigateToSearchFragment(this@CategoryBangumisFragment)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    /**
     * 这里早期接口设计有问题
     *
     */
    @Suppress("UNCHECKED_CAST")
    private fun addObserver() = with(viewModel) {
        categoryBangumis.observe(this@CategoryBangumisFragment) {
            when (val adapter = mRecyclerView.adapter) {
                is ZfCategoryBanumiAdapter -> adapter.submitList(it as PagedList<CategoryBangumi>)

                is DiliCategoryBanumiAdapter -> adapter.submitList(it as List<DiliBangumi>)
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

        viewModel.itemClickPosition.observe { position ->
            val clickBangumi: CategoryBangumi? = (mRecyclerView.adapter as ICategoryAdapter).getBagnumi(position)
            clickBangumi?.let {
                val intent = Intent(requireActivity(), PlayerActivity::class.java).apply {
                    putExtra(ARG_ID, clickBangumi.id)
                    putExtra(ARG_BANGUMI_SOURCE, clickBangumi.source.name)
                }
                startActivityForResult(intent, position)
            }
        }

        getCategoryBangumis(categoryArg)
    }

    /**
     * 追番状态可能在[PlayerActivity]改变, 改变的值会回传回来, 再修改对应Item状态
     * requestCode为点击启动[PlayerActivity]item索引
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 点击条目的索引为requestCode
        if (requestCode == viewModel.itemClickPosition.value) {
            data?.let {
                val isFollow = it.getBooleanExtra(FOLLOW_RESULT_KEY, false)

                // 默认RecyclerView的Adapter都实现ICategoryAdapter接口
                (mRecyclerView.adapter as ICategoryAdapter).onFollowStateChange(requestCode, isFollow)
            }
        }
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
