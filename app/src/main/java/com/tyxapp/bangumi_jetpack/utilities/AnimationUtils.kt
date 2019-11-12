package com.tyxapp.bangumi_jetpack.utilities

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R

fun Int.toPx(): Int {
    val density = BangumiApp.getContext().resources.displayMetrics.density
    return (this * density + 0.5f).toInt()
}

fun View.translationFadeIn(
    duration: Long = 375,
    startTranslationY: Float = 250f
) {
    alpha = 0f
    translationY = startTranslationY
    animate().alpha(1.0f)
        .translationY(0f)
        .setInterpolator(LinearOutSlowInInterpolator())
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

inline fun Animation.setListener(
    crossinline startAction: (Animation?) -> Unit? = {},
    crossinline endAction: (Animation?) -> Unit = {}
) {

    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            endAction(animation)
        }

        override fun onAnimationStart(animation: Animation?) {
            startAction(animation)
        }

    })
}

inline fun ViewPropertyAnimator.setListener(
    crossinline actionStart: (Animator?) -> Unit = {},
    crossinline actionEnd: (Animator?) -> Unit = {}
): ViewPropertyAnimator {
    return this.apply {
        setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                actionEnd(animation)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                actionStart(animation)
            }

        })
    }
}

inline fun View.fadeIn(
    duration: Long = 250,
    crossinline startAction: (Animator?) -> Unit = { this.isGone(false) },
    crossinline endAction: (Animator?) -> Unit = {  }
) {
    this.alpha = 0f
    this.animate()
        .alpha(1.0f)
        .setDuration(duration)
        .setListener(startAction, endAction)
        .start()
}

inline fun View.fadeOut(
    duration: Long = 250,
    crossinline startAction: (Animator?) -> Unit = {},
    crossinline endAction: (Animator?) -> Unit = { this.visibility = View.GONE }
) {

    if (!this.isVisible) return
    this.animate()
        .alpha(0f)
        .setListener (startAction, endAction)
        .setDuration(duration)
        .start()
}

fun View.slideIn(duration: Long = 150) {
    isGone(false)
    val slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
    slideInAnimation.duration = duration
    startAnimation(slideInAnimation)
}

fun View.slideOut(duration: Long = 150) {
    val slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_left)
    slideOutAnimation.duration = duration
    slideOutAnimation.setListener(endAction = {
        this.isGone(true)
    })
    startAnimation(slideOutAnimation)
}
