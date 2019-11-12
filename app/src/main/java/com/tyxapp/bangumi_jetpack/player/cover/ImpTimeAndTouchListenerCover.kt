package com.tyxapp.bangumi_jetpack.player.cover

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.kk.taurus.playerbase.player.OnTimerUpdateListener
import com.kk.taurus.playerbase.receiver.BaseCover
import com.kk.taurus.playerbase.touch.OnTouchGestureListener

abstract class ImpTimeAndTouchListenerCover(
    context: Context
) : BaseCover(context), OnTimerUpdateListener, OnTouchGestureListener {
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

    override fun onTimerUpdate(curr: Int, duration: Int, bufferPercentage: Int) {

    }

}