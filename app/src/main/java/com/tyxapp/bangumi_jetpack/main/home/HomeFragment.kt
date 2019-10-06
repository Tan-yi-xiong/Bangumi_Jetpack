package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.HomeFragmentBinding
import com.tyxapp.bangumi_jetpack.utilities.info

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bind = HomeFragmentBinding.inflate(inflater, container, false)
        val navController = bind.root.findViewById<View>(R.id.homeContent).findNavController()
        bind.bottomNavigationView.setupWithNavController(navController)

        //侧栏开关
        val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawerlayout)
        bind.bottonMenu.setOnClickListener { drawerLayout.openDrawer(Gravity.LEFT) }

        //侧栏导航设置
        val navigationView = activity!!.findViewById<NavigationView>(R.id.navigationView)
        val mainNavController = activity!!.findViewById<View>(R.id.main_content).findNavController()
        navigationView.setupWithNavController(mainNavController)

        return bind.root
    }

}
