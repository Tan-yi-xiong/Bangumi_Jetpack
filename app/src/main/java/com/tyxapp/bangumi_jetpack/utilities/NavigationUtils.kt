package com.tyxapp.bangumi_jetpack.utilities

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.home.CategoryBangumisFragment
import com.tyxapp.bangumi_jetpack.main.search.SearchFragment
import com.tyxapp.bangumi_jetpack.player.*
import org.jetbrains.anko.intentFor

const val HOME_TO_CB_STASK_NAME = "HOME_TO_CB_STASK_NAME"

/**
 * 判断是否在返回栈
 *
 */
fun FragmentManager.isOnBackStask(name: String): Boolean {
    for (i in 0 until backStackEntryCount) {
        val backSstackName = getBackStackEntryAt(i).name
        if (backSstackName == name) {
            return true
        }
    }
    return false
}

fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()

}

/**
 * 跳转到PlayerActivity
 *
 */
fun Activity.startPlayerActivity(id: String, bangumiSource: String) {
    startActivity(intentFor<PlayerActivity>(
        ARG_ID to id,
        ARG_BANGUMI_SOURCE to bangumiSource
    ))
}

fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

/**
 * HomeFragmen跳到CategoryBangumisFragment
 *
 */
fun MainActivity.navigateToCategoryBangumisFragment(arg: String) {
    val homeFragment = supportFragmentManager.findFragmentByTag(getFragmentTag(0))
    if (homeFragment != null && !homeFragment.isHidden) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            hide(homeFragment)
            add(R.id.main_content, CategoryBangumisFragment.newInstance(arg))
            addToBackStack(HOME_TO_CB_STASK_NAME)
        }
    }
}

fun Activity.navigateToLoaclPlayerActivity(url: String) {
    startActivity(intentFor<LocalPlayerActivity>(LOCAL_VIDEO_URL_KEY to url))
}

const val SEARCHFRAGMENT_STACK_NAME = "SEARCHFRAGMENT_STACK_NAME"
fun MainActivity.navigateToSearchFragment(fragment: Fragment): Boolean {
    return if (!fragment.isDetached && !fragment.isHidden) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.nav_default_enter_anim,
                R.anim.nav_default_exit_anim,
                R.anim.nav_default_pop_enter_anim,
                R.anim.nav_default_pop_exit_anim
            )
            hide(fragment)
            add(R.id.main_content, SearchFragment.newInstance())
            addToBackStack(SEARCHFRAGMENT_STACK_NAME)
        }
        true
    } else {
        false
    }
}

fun getFragmentTag(index: Int): String = "fragment$index"