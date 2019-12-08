package com.tyxapp.bangumi_jetpack.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import com.tyxapp.bangumi_jetpack.utilities.toPx
import org.jetbrains.anko.attr

private const val DOT_COUNT = 3
private const val DURATION = 500L
private const val DISTANCE = 30

class PlayerLoadingView : LinearLayout {
    private val dots = arrayOfNulls<ImageView>(DOT_COUNT)
    private var onLayoutReach = false
    private val mDotAnimators = arrayOfNulls<Animator>(DOT_COUNT)
    private val DOT_SIZE = 8.toPx()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {

        gravity = Gravity.CENTER
        orientation = HORIZONTAL

        val dot = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ContextCompat.getColor(context, R.color.colorAccent))
            setSize(DOT_SIZE, DOT_SIZE)
        }

        for (i in 0 until DOT_COUNT) {
            val imageView = ImageView(context)
            imageView.setImageDrawable(dot)
            addView(imageView)
            dots[i] = imageView
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!onLayoutReach) {
            onLayoutReach = true
            val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            layoutParams.marginEnd = 5.toPx()
            dots.forEach {
                it?.layoutParams = layoutParams
            }
            if (isVisible) {
                startLoadingAnimation()
            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.VISIBLE) {
            startLoadingAnimation()
        } else {
            stopLoadingAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        for (i in 0 until DOT_COUNT) {
            if (mDotAnimators[i] == null) {
                mDotAnimators[i] = createAnimation(dots[i]!!, i)
            }
            mDotAnimators[i]!!.start()
        }
    }

    private fun stopLoadingAnimation() {
        for (i in 0 until DOT_COUNT) {
            mDotAnimators[i]?.let {
                it.removeAllListeners()
                it.cancel()
                it.end()
            }
            mDotAnimators[i] = null
        }
    }

    private fun createAnimation(view: ImageView, position: Int): Animator {
        view.translationY = DISTANCE.toFloat()
        val holder = PropertyValuesHolder.ofFloat(
            ImageView.TRANSLATION_Y,
            -DISTANCE.toFloat()
        )
        val animator = ObjectAnimator.ofPropertyValuesHolder(view, holder)
        animator.apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = DURATION
            repeatMode = ObjectAnimator.REVERSE
            startDelay = (position * 166).toLong()
        }
        return animator
    }
}