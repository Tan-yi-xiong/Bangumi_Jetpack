package com.tyxapp.bangumi_jetpack.utilities

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.getSystemService

fun getNavigationBarHeight(context: Context): Int {
    //全屏包括导航栏高度
    val windowManager = context.getSystemService<WindowManager>()!!
    val displayMetrics = DisplayMetrics().apply {
        windowManager.defaultDisplay.getRealMetrics(this)
    }

    //全屏不包括导航栏高度
    val noNavDisplayMetrics = context.resources.displayMetrics

    return if (displayMetrics.heightPixels == noNavDisplayMetrics.heightPixels) {
        if (displayMetrics.widthPixels == noNavDisplayMetrics.widthPixels) {
            0
        } else {
            displayMetrics.widthPixels - noNavDisplayMetrics.widthPixels
        }
    } else {
        displayMetrics.heightPixels - noNavDisplayMetrics.heightPixels
    }
}