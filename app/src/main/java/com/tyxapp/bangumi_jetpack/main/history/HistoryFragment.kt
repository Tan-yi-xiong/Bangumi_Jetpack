package com.tyxapp.bangumi_jetpack.main.history

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ToolbarListFragment
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.NAVIGATION_VIEW_STACK_NAME
import com.tyxapp.bangumi_jetpack.utilities.*

class HistoryFragment : ToolbarListFragment() {
    private val viewModel: HistoryViewModel by viewModels {
        InjectorUtils.provideHistoryViewModelFactory()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            return AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_enter_anim).apply {
                setListener {
                    addObservor()
                }
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().supportFragmentManager.popBackStack(
                NAVIGATION_VIEW_STACK_NAME,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            showLoading()
            initView()
        }
    }

    private fun addObservor() {
        with(viewModel) {
            historyBangumis.observe {
                (mRecyclerView.adapter as HistoryAdapter).submitList(it)
                if (it.isEmpty()) {
                    showEmpty()
                } else {
                    if (!mRecyclerView.isVisible) {
                        mRecyclerView.translationFadeIn(800)
                    }
                    showContent()
                }
            }
        }
    }

    private fun initView() {
        with(mToolBar) {
            setNavigationIcon(R.drawable.ic_menu)
            setNavigationOnClickListener {
                requireActivity().findViewById<DrawerLayout>(R.id.drawerlayout).openDrawer(Gravity.LEFT)
            }
            title = getString(R.string.menu_title_history)
            setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.search) {
                    (requireActivity() as MainActivity).navigateToSearchFragment(parentFragment!!)
                    true
                } else {
                    false
                }
            }
        }

        mRecyclerView.adapter = HistoryAdapter()
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }
}
