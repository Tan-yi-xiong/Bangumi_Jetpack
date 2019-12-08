package com.tyxapp.bangumi_jetpack.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.tyxapp.bangumi_jetpack.R
import kotlin.math.min

/**
 * 圆形填充色View
 *
 */
class ColorIndicator : View {
    private var paint = Paint()
    var indicatorColor: Int = Color.BLACK
        set(value) {
            paint.color = value
            invalidate()
            field = value
        }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context!!.obtainStyledAttributes(attrs, R.styleable.ColorIndicator)
        indicatorColor = typedArray.getColor(0, indicatorColor)
        typedArray.recycle()

        paint.isAntiAlias = true
        paint.color = indicatorColor
    }

    override fun onDraw(canvas: Canvas?) {
        val radius = (min(measuredHeight, measuredWidth) / 2).toFloat()
        paint.strokeWidth = radius

        canvas?.drawCircle(radius, radius, radius, paint)
    }

}