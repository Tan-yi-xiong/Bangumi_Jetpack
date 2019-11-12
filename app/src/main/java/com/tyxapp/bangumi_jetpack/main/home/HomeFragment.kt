package com.tyxapp.bangumi_jetpack.main.home

import android.os.Bundle
import android.util.SparseArray
import android.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.HomeFragmentBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.*

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)

        binding.searchBar.setOnClickListener {
            (requireActivity() as MainActivity).navigateToSearchFragment(parentFragment!!)
        }

        //侧栏开关
        val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawerlayout)
        binding.bottonMenu.setOnClickListener { drawerLayout.openDrawer(Gravity.LEFT) }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBottomNavigationView()
    }

    private fun initBottomNavigationView() {
        val navgGraphIds = listOf(
            R.navigation.bangumi,
            R.navigation.category,
            R.navigation.timetable
        )
        binding.bottomNavigationView.setupWithNavController(
            navgGraphIds,
            childFragmentManager,
            R.id.homeContent
        )
    }

}

private fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int
) {
    var fristNavGraphId = 0
    val navGraphWithTag = SparseArray<String>()
    var currentFragmentTag = ""

    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        val navHostFragment = obtainNavHostFragment(
            fragmentManager, fragmentTag,
            navGraphId, containerId
        )

        val graphId = navHostFragment.navController.graph.id

        if (index == 0) {
            fristNavGraphId = graphId
        }

        navGraphWithTag.append(graphId, fragmentTag)


        if (this.selectedItemId == graphId) {
            currentFragmentTag = fragmentTag
            attachNavHostFragment(
                fragmentManager,
                navHostFragment,
                true
            )
        } else {
            detachNavHostFragment(
                fragmentManager,
                navHostFragment
            )
        }
    }

    val fristFragmentTag = navGraphWithTag[fristNavGraphId]
    var isFristFragment = currentFragmentTag == fristFragmentTag

    setOnNavigationItemSelectedListener { item: MenuItem ->
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val selectFragmentTag = navGraphWithTag[item.itemId]
            if (selectFragmentTag != currentFragmentTag) {
                //恢复到初始状态
                fragmentManager.popBackStack(
                    fristFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )

                //第一个fragment就是初始状态, 不用处理
                if (selectFragmentTag != fristFragmentTag) {
                    val selectFragment = fragmentManager.findFragmentByTag(selectFragmentTag)!!

                    fragmentManager.commit {
                        setCustomAnimations(
                            R.anim.nav_default_enter_anim,
                            R.anim.nav_default_exit_anim,
                            R.anim.nav_default_pop_enter_anim,
                            R.anim.nav_default_pop_exit_anim
                        )
                        hide(fragmentManager.findFragmentByTag(fristFragmentTag)!!)//隐藏主fragment
                        attach(selectFragment)//attach选中的
                        addToBackStack(fristFragmentTag)
                        setPrimaryNavigationFragment(selectFragment)
                        setReorderingAllowed(true)//优化事务, 会把多余操作去除, 不开启有bug
                    }
                }
                currentFragmentTag = selectFragmentTag
                isFristFragment = currentFragmentTag == fristFragmentTag
                true
            } else {
                false
            }
        }
    }

    /**
     * 监听返回栈事件, 实现用户按下返回键跳到第一个页面
     *
     */
    fragmentManager.addOnBackStackChangedListener {
        //当前不是第一个页面, 同时也没有找到返回栈, 表示用户按下返回键
        if (!isFristFragment && !fragmentManager.isOnBackStask(fristFragmentTag)) {
            this.selectedItemId = fristNavGraphId
        }
    }
}
