package com.tyxapp.bangumi_jetpack.main.mydownload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.ToolbarListFragment
import com.tyxapp.bangumi_jetpack.data.UIStata
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.mydownload.adapter.DownloadDetailAdapter
import com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels.DownloadDetailViewModel
import com.tyxapp.bangumi_jetpack.utilities.InjectorUtils
import com.tyxapp.bangumi_jetpack.utilities.navigateToSearchFragment
import com.tyxapp.bangumi_jetpack.utilities.setListener
import com.tyxapp.bangumi_jetpack.views.snack

class DownloadDetailFragment : ToolbarListFragment() {
    private val viewModel: DownloadDetailViewModel by viewModels {
        InjectorUtils.provideDownloadDetailViewModelFactory()
    }

    private val args: DownloadDetailFragmentArgs by navArgs()

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_enter_anim).apply {
                setListener {
                    addObserver()
                }
            }
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
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

    private fun addObserver() {
        viewModel.bangumiDownLoadVideos.observe {
            if (it.isEmpty()) {
                showEmpty()
            } else {
                (mRecyclerView.adapter as DownloadDetailAdapter).submitList(it)
                showContent()
            }

        }

        viewModel.alertMessage.observe {
            mRecyclerView.snack(it)
        }


        viewModel.uiStata.observe {
            when (it) {
                UIStata.LOADING -> showLoading()
            }
        }

        viewModel.loadingData(args.bangumiId, args.bangumiSource)
    }

    private fun initView() {
        mRecyclerView.adapter =
            DownloadDetailAdapter(viewModel)

        with(mToolBar) {
            setNavigationOnClickListener { findNavController().popBackStack() }
            title = "下载详情"
            setOnMenuItemClickListener { menuitem ->
                when (menuitem.itemId) {
                    R.id.search -> (requireActivity() as MainActivity).navigateToSearchFragment(
                        parentFragment!!
                    )
                }
                true
            }
        }
    }
}