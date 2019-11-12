package com.tyxapp.bangumi_jetpack.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.kk.taurus.playerbase.widget.BaseVideoView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlin.math.max

class ParallaxVideoView(
    context: Context, attrs: AttributeSet?
) : BaseVideoView(context, attrs) {
    private var minOffset: Int = 0
    private var videoOffset: Int = 0
    private val parallaxFactor = -0.5f

    private var mOffsetListener: ((Int) -> Unit)? = null

    var offset: Int
        get() = translationY.toInt()
        set(offset) {
            mOffsetListener?.invoke(offset)
            val offsetin = max(minOffset, offset)
            if (offsetin.toFloat() != translationY) {
                translationY = offsetin.toFloat()
                videoOffset = (offsetin * parallaxFactor).toInt()
                postInvalidateOnAnimation()
            }
        }

    override fun onDraw(canvas: Canvas) {
        if (videoOffset != 0) {
            val saveCount = canvas.saveCount
            canvas.translate(0f, videoOffset.toFloat())
            super.onDraw(canvas)
            canvas.restoreToCount(saveCount)
        } else {
            super.onDraw(canvas)
        }
    }

    fun setOnOffsetListener(offsetListener: (Int) -> Unit) {
        mOffsetListener = offsetListener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > minimumHeight) {
            minOffset = minimumHeight - h
        }
    }

}