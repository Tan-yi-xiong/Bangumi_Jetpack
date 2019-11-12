package com.tyxapp.bangumi_jetpack.player.cover

import android.content.Context
import android.view.MotionEvent
import com.kk.taurus.playerbase.receiver.BaseCover
import com.kk.taurus.playerbase.touch.OnTouchGestureListener

abstract class ImpTouchGestureListener(
    context: Context
) : BaseCover(context), OnTouchGestureListener {
    override fun onEndGesture() {

    }

    override fun onSingleTapUp(event: MotionEvent?) {
    }

    override fun onDown(event: MotionEvent?) {
    }

    override fun onDoubleTap(event: MotionEvent?) {
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) {
    }
}