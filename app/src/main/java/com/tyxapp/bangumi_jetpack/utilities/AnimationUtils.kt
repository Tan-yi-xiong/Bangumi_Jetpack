package com.tyxapp.bangumi_jetpack.utilities

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.animation.doOnStart
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R

fun Int.toPx(): Int {
    val density = BangumiApp.getContext().resources.displayMetrics.density
    return (this * density + 0.5f).toInt()
}

fun View.tranFadeIn(
        duration: Long = 800,
        startTranslationY: Float = 200f
) {
    alpha = 0f
    translationY = startTranslationY
    animate().alpha(1.0f)
            .translationY(0f)
            .duration = duration
}

fun View.popAnimation(
        startDelay: Long = 0,
        duration: Long = 500
) {
    scaleX = 0f
    scaleY = 0f
    alpha = 0f
    with(animate()) {
        scaleX(1.0f)
        scaleY(1.0f)
        alpha(1.0f)
        this.duration = duration
        interpolator = AnimationUtils.loadInterpolator(context, android.R.interpolator.overshoot)
        this.startDelay = startDelay
    }
}

inline fun Animation.doOnEnd(crossinline endAction: (Animation?) -> Unit) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            endAction(animation)
        }

        override fun onAnimationStart(animation: Animation?) {
        }

    })
}