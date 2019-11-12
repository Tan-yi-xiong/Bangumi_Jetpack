package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.State
import com.tyxapp.bangumi_jetpack.databinding.TimeTableFragmentBinding
import com.tyxapp.bangumi_jetpack.main.home.adapter.TimeTableAdapter
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.TimeTableViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.translationFadeIn

class TimeTableFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var viewModel: TimeTableViewModel
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var binding: TimeTableFragmentBinding
    private lateinit var mTabLayout: TabLayout
    private lateinit var errorLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimeTableFragmentBinding.inflate(inflater, container, false)
        mTabLayout = binding.tabLayout
        mRecyclerView = binding.recyclerView
        errorLayout = binding.root.findViewById(R.id.errorView)
        val tabTexts = requireContext().resources.getStringArray(R.array.week)

        errorLayout.setOnClickListener { viewModel.refresh() }

        tabTexts.forEachIndexed { index, text ->
            val tab = mTabLayout.newTab().setText(text)
            tab.tag = index
            mTabLayout.addTab(tab)
        }
        mTabLayout.tabSelect { tab ->
            tab?.let { viewModel.onTabSelect(tab.tag as Int) }
        }

        mRecyclerView.adapter = TimeTableAdapter()

        mainViewModel.homeDataRepository.observeForever {

        }
        mainViewModel.homeDataRepository.observe(this) {
            if (!::viewModel.isInitialized) {
                viewModel = ViewModelProviders.of(
                    this,
                    InjectorUtils.provideTimeTableViewModelFactory(it)
                ).get(TimeTableViewModel::class.java)
                addObserver()
            } else {
                viewModel.replaceRepository(it)
            }
        }
        return binding.root
    }

    private fun addObserver() = with(viewModel) {

        dayBangumis.observe(this@TimeTableFragment) {
            (mRecyclerView.adapter as TimeTableAdapter).submitList(it)
        }

        currentWeekDayValue.observe(this@TimeTableFragment) {
            mTabLayout.getTabAt(it)?.select()
        }

        uiStata.observeForever {
            when (it.netWordState.state) {
                State.LOADING -> showLoading()

                State.SUCCESS -> {
                    //进场动画
                    if (!mRecyclerView.isVisible) {
                        mRecyclerView.translationFadeIn()
                    }
                    showContent()
                }

                State.ERROR -> showError()
            }
        }

    }

    private fun showContent() {
        binding.showContent = true
        binding.showLoading = false
        binding.showError = false
    }

    private fun showError() {
        binding.showContent = false
        binding.showLoading = false
        binding.showError = true
    }

    private fun showLoading() {
        binding.showContent = false
        binding.showLoading = true
        binding.showError = false
    }


    private inline fun TabLayout.tabSelect(crossinline action: (TabLayout.Tab?) -> Unit) {
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                action(tab)
            }

        })
    }
}


