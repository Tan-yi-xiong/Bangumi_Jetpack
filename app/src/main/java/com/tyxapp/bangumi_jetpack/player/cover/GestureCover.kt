package com.tyxapp.bangumi_jetpack.player.cover

import android.content.Context
import android.graphics.Point
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kk.taurus.playerbase.event.EventKey
import com.kk.taurus.playerbase.utils.TimeUtil
import com.tyxapp.bangumi_jetpack.databinding.LayoutGestureCoverBinding
import com.tyxapp.bangumi_jetpack.player.UPDATE_SEEK_CODE
import com.tyxapp.bangumi_jetpack.player.UPDATE_SEEK_KEY
import com.tyxapp.bangumi_jetpack.utilities.isGone
import com.tyxapp.bangumi_jetpack.utilities.toPx
import kotlin.math.abs
import kotlin.math.max

private const val BASE_SEEK = 30f
private const val MAX_BRIGHTNESS = 255

class GestureCover(
    private val mActivity: AppCompatActivity
) : ImpTouchGestureListener(mActivity) {

    private lateinit var bind: LayoutGestureCoverBinding
    private lateinit var mBrightnessProgressView: View
    private lateinit var mVolumeProgressView: View
    private lateinit var mVideoVolumeSeekBar: SeekBar
    private lateinit var mBrightnessSeekBar: SeekBar
    private lateinit var mSeekText: TextView

    private val mBundle = Bundle()

    private var seekStep = 30f
    private var subjoinValue: Float = 0f //快进值
    private var hasSeekTo = false // 是否进行过进度调节
    private var downY = 0f
    private var mBrightness = //当前亮度
        Settings.System.getInt(mActivity.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 125)

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val volumeAndBrightnessStep by lazy(LazyThreadSafetyMode.NONE) {
        val point = Point()
        mActivity.windowManager.defaultDisplay.getSize(point)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        (point.y / 3) / maxVolume
    }
    private val brightnessStepValue =
        MAX_BRIGHTNESS / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    private var isVolumeEvent = false
    private var isDownToStateBar = false

    private val stateBarHeight by lazy {
        val resId = mActivity.resources.getIdentifier("status_bar_height", "dimen", "android")
        val height = mActivity.resources.getDimension(resId)
        if (height <= 0) 10.toPx().toFloat() else height
    }

    private val screenWidth: Int
        get() = Point().run {
            mActivity.windowManager.defaultDisplay.getSize(this)
            max(this.x, this.y)
        }


    override fun onCreateCoverView(context: Context?): View {
        bind = LayoutGestureCoverBinding.inflate(LayoutInflater.from(context)).apply {
            mBrightnessProgressView = brightnessProgressView
            mVolumeProgressView = volumeProgressView
            mVideoVolumeSeekBar = videoVolumeSeekBar
            mBrightnessSeekBar = brightnessSeekBar
            mSeekText = skipText

            lifecycleOwner = mActivity
        }
        return bind.root
    }

    /**
     * 判断手指放下时在屏幕的左边还是右面和是否在状态栏范围
     *
     */
    override fun onDown(event: MotionEvent?) {
        event ?: return
        downY = event.y
        seekStep = BASE_SEEK * (2960 / screenWidth)
        if (downY <= stateBarHeight) {
            isDownToStateBar = true
        } else {
            isDownToStateBar = false
            isVolumeEvent = event.x >= screenWidth / 2 // 左边为亮度调节, 右边为声音调节
        }
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) {
        if (isDownToStateBar) return // 手指在状态栏范围放下不处理
        val dx = e2!!.x - e1!!.x
        val dy = e2.y - e1.y
        val absDistanceX = abs(dx)
        val absDistanceY = abs(dy)
        if (absDistanceX > 10 || absDistanceY > 10) {
            view.isGone(false) // 显示手势视图
            if (absDistanceX > absDistanceY) { // 左右滑动事件为视频进度变换
                if (bind.showVolumeView == true || bind.showBrightnessView == true) {
                    return
                }
                seekEvent(dx)
            } else { // 上下滑动事件
                if (bind.showSkipText == true) return
                if (isVolumeEvent) {
                    volumeEvent(e2)
                } else {
                    brightnessEvent(e2)
                }
            }
        }
    }

    override fun onEndGesture() {
        view.isGone(true)
        hindAll()
        if (hasSeekTo) { // 手势释放后是进度调节事件的话快进到最后调节的进度
            hasSeekTo = false
            mBundle.putInt(EventKey.INT_DATA, subjoinValue.toInt())
            requestSeek(mBundle)
        }
    }

    /**
     * 亮度调节
     */
    private fun brightnessEvent(e2: MotionEvent) {
        if (bind.showBrightnessView != true) {
            showBrightnessView()
        }
        val dy = e2.y - downY
        if (volumeAndBrightnessStep <= abs(dy)) {
            downY = e2.y
            if (dy < 0) {
                mBrightness += brightnessStepValue
                if (mBrightness > MAX_BRIGHTNESS) {
                    mBrightness = MAX_BRIGHTNESS
                }
            } else if (dy > 0) {
                mBrightness -= brightnessStepValue
                if (mBrightness < 0) {
                    mBrightness = 0
                }
            }
        }

        val attributes = mActivity.window.attributes
        attributes.screenBrightness = mBrightness / 255.0f
        mActivity.window.attributes = attributes

        mBrightnessSeekBar.progress = mBrightness
        mBrightnessSeekBar.max = MAX_BRIGHTNESS
    }

    /**
     * 声音调节
     */
    private fun volumeEvent(e2: MotionEvent) {
        if (bind.showVolumeView != true) {
            showVolumeView()
        }
        val dy = e2.y - downY
        if (volumeAndBrightnessStep <= abs(dy)) {
            downY = e2.y
            if (dy < 0) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    0
                )
            } else {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    0
                )
            }
        }

        mVideoVolumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mVideoVolumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * 视频进度调节
     */
    private fun seekEvent(distanceX: Float) {
        hasSeekTo = true
        if (bind.showSkipText != true) {
            showSeekText()
        }

        val currentTime = playerStateGetter?.currentPosition ?: 0
        subjoinValue = distanceX * seekStep + currentTime
        val currentTimeText = TimeUtil.getTimeSmartFormat(currentTime.toLong())
        val subjoinTimeText = TimeUtil.getTimeSmartFormat(subjoinValue.toLong())
        mSeekText.text = "$currentTimeText/$subjoinTimeText"

        //通知ControlCover更新进度条
        mBundle.putInt(UPDATE_SEEK_KEY, subjoinValue.toInt())
        notifyReceiverPrivateEvent(ControlCover::class.java.simpleName, UPDATE_SEEK_CODE, mBundle)
    }

    private fun showSeekText() {
        bind.showSkipText = true
        bind.showBrightnessView = false
        bind.showVolumeView = false
    }

    private fun showBrightnessView() {
        bind.showSkipText = false
        bind.showBrightnessView = true
        bind.showVolumeView = false
    }

    private fun showVolumeView() {
        bind.showSkipText = false
        bind.showBrightnessView = false
        bind.showVolumeView = true
    }

    private fun hindAll() {
        bind.showSkipText = false
        bind.showBrightnessView = false
        bind.showVolumeView = false
    }

    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {

    }

    override fun onReceiverEvent(eventCode: Int, bundle: Bundle?) {

    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {

    }

    override fun onReceiverUnBind() {
        bind.unbind()
        super.onReceiverUnBind()
    }

    override fun getCoverLevel(): Int {
        return levelLow(2)
    }
}