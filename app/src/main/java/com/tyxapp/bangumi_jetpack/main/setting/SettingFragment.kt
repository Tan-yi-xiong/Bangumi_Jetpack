package com.tyxapp.bangumi_jetpack.main.setting

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.utilities.LOGI

class SettingFragment : Fragment() {
    private lateinit var mToolBar: Toolbar

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_enter_anim)
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        mToolBar = view.findViewById(R.id.toolBar)
        mToolBar.setTitle(R.string.title_setting)
        mToolBar.setNavigationOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawerlayout).openDrawer(Gravity.LEFT)
        }
        childFragmentManager.commit { replace(R.id.content, MainSettingFragment.getInstance()) }
        return view
    }
}