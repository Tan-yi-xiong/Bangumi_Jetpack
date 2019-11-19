package com.tyxapp.bangumi_jetpack.main

import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils

/**
 * 禁止当前Fragment滑出[MainActivity]的DrawerLayout, 有些Fragment并不能设计成能滑出DrawerLayout
 *
 */
abstract class UnSkidFragment : Fragment() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onStart() {
        super.onStart()
        drawerLayout = requireActivity().findViewById(R.id.drawerlayout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (PrefUtils.getDrawerLayoutIsUnLock()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    /**
     * 再次显示的时候要再次复位滑动模式, 防止其他fragment修改
     *
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

}