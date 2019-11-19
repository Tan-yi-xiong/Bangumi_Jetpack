package com.tyxapp.bangumi_jetpack.main.mydownload

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ToolbarListFragment
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.mydownload.adapter.DownloadAdapter
import com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels.MyDownloadViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.navigateToSearchFragment
import com.tyxapp.bangumi_jetpack.utilities.setListener
import com.tyxapp.bangumi_jetpack.views.snack

class MyDownloadFragment : ToolbarListFragment() {

    private val viewModel: MyDownloadViewModel by viewModels {
        InjectorUtils.provideMyDownloadViewModelFactory()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(context, R.anim.nav_default_enter_anim).apply {
                setListener {
                    addObserver()
                }
            }
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    private fun addObserver() {
        viewModel.downLoadBangumis.observe {
            if (it.isEmpty()) {
                showEmpty()
            } else {
                (mRecyclerView.adapter as DownloadAdapter).submitList(it)
                showContent()
            }
        }

        viewModel.alertMessage.observe {
            mRecyclerView.snack(it)
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
        mRecyclerView.adapter =
            DownloadAdapter(viewModel)

        with(mToolBar) {
            mToolBar.menu.removeItem(R.id.search)
            mToolBar.setNavigationIcon(R.drawable.ic_menu)
            setTitle(R.string.menu_title_my_download)
            setNavigationOnClickListener {
                requireActivity().findViewById<DrawerLayout>(R.id.drawerlayout).openDrawer(Gravity.LEFT)
            }
            setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.search -> (requireActivity() as MainActivity).navigateToSearchFragment(parentFragment!!)
                }
                true
            }
        }

        showLoading()
    }

    companion object {
        fun newInstance() = MyDownloadFragment()
    }
}
