package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.TimeTableFragmentBinding
import com.tyxapp.bangumi_jetpack.main.home.adapter.TimeTableAdapter
import com.tyxapp.bangumi_jetpack.main.viewmodels.MainViewModel
import com.tyxapp.bangumi_jetpack.main.viewmodels.TimeTableViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.tranFadeIn

class TimeTableFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: TimeTableViewModel by viewModels {
        InjectorUtils.provideTimeTableViewModelFactory(mainViewModel.homeDataRepository.value!!)
    }
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var binding: TimeTableFragmentBinding
    private lateinit var mTabLayout: TabLayout

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = TimeTableFragmentBinding.inflate(inflater, container, false)
        mTabLayout = binding.tabLayout
        mRecyclerView = binding.recyclerView
        val tabTexts = requireContext().resources.getStringArray(R.array.week)

        tabTexts.forEachIndexed { index, text ->
            val tab = mTabLayout.newTab().setText(text)
            tab.tag = index
            mTabLayout.addTab(tab)
        }
        mTabLayout.tabSelect { tab ->
            if (viewModel.firstLoadingFinish.value == true) {
                tab?.let { viewModel.onTabSelect(tab.tag as Int) }
            }
        }
        mRecyclerView.adapter = TimeTableAdapter(viewModel)

        addObserver()
        return binding.root
    }

    private fun addObserver() = with(viewModel) {

        dayBangumis.observe {
            (mRecyclerView.adapter as TimeTableAdapter).submitList(it)
            showContent(true)
            if (firstLoadingFinish.value != true) {
                mRecyclerView.tranFadeIn()
            }
        }

        currentWeekDayValue.observe {
            mTabLayout.getTabAt(it)?.select()
        }

        error.observe {
            Toast.makeText(requireContext(), getString(R.string.text_timetable_error), Toast.LENGTH_SHORT).show()
        }

        loading.observe { isLoading ->
            if (isLoading) showContent(!isLoading)
        }

    }

    fun showContent(boolean: Boolean) {
        binding.hindContent = !boolean
        binding.loading = boolean
    }

    private inline fun <reified T> LiveData<T>.observe(crossinline action: (T) -> Unit) {
        observe(this@TimeTableFragment, Observer<T> { action(it) })
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


