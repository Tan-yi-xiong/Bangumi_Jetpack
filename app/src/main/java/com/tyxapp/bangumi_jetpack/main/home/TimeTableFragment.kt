package com.tyxapp.bangumi_jetpack.main.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.State
import com.tyxapp.bangumi_jetpack.databinding.TimetableFragmentBinding
import com.tyxapp.bangumi_jetpack.main.MainViewModel
import com.tyxapp.bangumi_jetpack.main.home.adapter.TimeTablePagerAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.TimeTableViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import com.tyxapp.bangumi_jetpack.utilities.translationFadeIn
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class TimeTableFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var viewModel: TimeTableViewModel
    private lateinit var mViewpager: ViewPager2
    private lateinit var binding: TimetableFragmentBinding
    private lateinit var mTabLayout: TabLayout
    private lateinit var errorLayout: View
    private lateinit var mediator: TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimetableFragmentBinding.inflate(inflater, container, false)
        mTabLayout = binding.tabLayout
        mViewpager = binding.viewpager
        errorLayout = binding.root.findViewById(R.id.errorView)

        errorLayout.setOnClickListener { viewModel.refresh() }


        mainViewModel.homeDataRepository.observe(this) {
            if (!::viewModel.isInitialized) {
                viewModel = ViewModelProviders.of(
                    this,
                    InjectorUtils.provideTimeTableViewModelFactory(it)
                ).get(TimeTableViewModel::class.java)
            } else {
                viewModel.replaceRepository(it)
            }
            addObserver()
        }

        initTabLayoutWithViewPage2()
        return binding.root
    }

    private fun initTabLayoutWithViewPage2() {
        mViewpager.adapter = TimeTablePagerAdapter()

        val tabTexts = requireContext().resources.getStringArray(R.array.week)
        mediator = TabLayoutMediator(mTabLayout, mViewpager) { tab, index ->
            tab.tag = index
            tab.text = tabTexts[index]
        }
    }

    private fun addObserver() = with(viewModel) {

        timeTableBagnumisLiveData.observe(this@TimeTableFragment) {
            val adapter = mViewpager.adapter as TimeTablePagerAdapter
            if (adapter.currentList.isEmpty()) {
                adapter.submitList(it)
                mViewpager.setCurrentItem(getTodayWeekValue(), false)
                mediator.attach()
                mViewpager.translationFadeIn()
            } else {
                adapter.submitList(it)
            }
        }

        uiStata.observe(this@TimeTableFragment) {
            when (it.netWordState.state) {
                State.LOADING -> showLoading()

                State.SUCCESS -> showContent()

                State.ERROR -> showError()
            }
        }

        viewModel.getTimeTableBagnumis()
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

    /**
     * 获取今天是今个星期的第几天
     *
     */
    private fun getTodayWeekValue(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.now().dayOfWeek.value - 1
        } else {
            val todayWeek =
                SimpleDateFormat("E", Locale.getDefault()).format(System.currentTimeMillis())
            val weekText = BangumiApp.getContext().resources.getStringArray(R.array.week)
            weekText.forEachIndexed { index, s ->
                if (s == todayWeek) {
                    return index
                }
            }
            return 0
        }
    }

    override fun onDestroyView() {
        binding.unbind()
        super.onDestroyView()
    }
}


