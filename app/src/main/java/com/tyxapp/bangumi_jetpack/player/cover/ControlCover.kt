package com.tyxapp.bangumi_jetpack.player.cover

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.kk.taurus.playerbase.entity.DataSource
import com.kk.taurus.playerbase.event.EventKey
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.receiver.IReceiverGroup
import com.kk.taurus.playerbase.utils.TimeUtil
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.databinding.LayoutCoverContralBinding
import com.tyxapp.bangumi_jetpack.player.*
import com.tyxapp.bangumi_jetpack.player.adapter.DANMU_ITEM
import com.tyxapp.bangumi_jetpack.player.adapter.DOWNLOAD_ITEM
import com.tyxapp.bangumi_jetpack.player.adapter.REPLAY_ITEM
import com.tyxapp.bangumi_jetpack.player.adapter.SPEED_ITEM
import com.tyxapp.bangumi_jetpack.player.bottomsheet.SettingBottomSheet
import com.tyxapp.bangumi_jetpack.player.bottomsheet.SpeedBottomSheet
import com.tyxapp.bangumi_jetpack.utilities.*
import com.tyxapp.bangumi_jetpack.views.alertBuilder
import com.tyxapp.bangumi_jetpack.views.noButton
import com.tyxapp.bangumi_jetpack.views.yesButton
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

private const val SINGLE_TAP_EVENT = 0
private const val HIDE_VIEV_EVENT = 1
private const val HIDE_DELAY = 5000L
private const val SINGLE_TAP_EVENT_DELAY = 130L
private const val HIDE_VIDEORECORD_VIEW_EVENT = 2

class ControlCover(
    private val mActivity: AppCompatActivity,
    private val viewModel: PlayerViewModel?
) : ImpTimeAndTouchListenerCover(mActivity), SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private lateinit var mSeekBar: SeekBar
    private lateinit var bind: LayoutCoverContralBinding
    private lateinit var mProgressTimeText: TextView
    private lateinit var mDurationTimeText: TextView
    private lateinit var mTopLayout: View
    private lateinit var mStateButton: ImageButton
    private lateinit var mNextJIButton: ImageButton
    private lateinit var mTimeTextView: TextView
    private lateinit var mBottomProgressBar: SeekBar
    private lateinit var mControlView: View
    private lateinit var mReturnToLastWatchView: View

    private val mBundle = Bundle()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                SINGLE_TAP_EVENT -> {
                    if (mControlView.isVisible) {
                        controlViewState(false)
                    } else {
                        mTimeTextView.text = dateFormat.format(System.currentTimeMillis()) //设置时间
                        sendEmptyMessageDelayed(HIDE_VIEV_EVENT, HIDE_DELAY) //5s后自动隐藏
                        controlViewState(true)
                    }
                }

                HIDE_VIEV_EVENT -> controlViewState(false)

                HIDE_VIDEORECORD_VIEW_EVENT -> mReturnToLastWatchView.slideOut(250)
            }
        }
    }

    private fun controlViewState(isShow: Boolean) {
        val isFullScreen = groupValue.getBoolean(FULL_SCREEN_KEY)
        if (isShow) {
            requestShowStateBar(true)
            mControlView.fadeIn()
            if (!isFullScreen) mBottomProgressBar.fadeOut()
        } else {
            mControlView.fadeOut()
            if (!isFullScreen) mBottomProgressBar.fadeIn()
            if (playerStateGetter?.state != IPlayer.STATE_PAUSED) requestShowStateBar(false)
        }
    }

    private val onGroupValueUpdateListener = object : IReceiverGroup.OnGroupValueUpdateListener {
        override fun onValueUpdate(key: String?, value: Any?) {
            if (key == FULL_SCREEN_KEY) {
                val isFullScreen = value as Boolean
                mBottomProgressBar.isGone(isFullScreen)

                mTopLayout.isGone(!isFullScreen)
                bind.fullScreenButton.isGone(isFullScreen)
                mNextJIButton.isGone(!isFullScreen)

                if (!isFullScreen && mControlView.isVisible) {
                    requestShowStateBar(true)
                }
            } else if (PERMISSION_REQUEST_KEY == key) {
                if (value as Boolean) {
                    viewModel?.downLoadVideo()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // 引导去设置权限
                        mActivity.alertBuilder(R.string.text_tips, R.string.text_permission) {
                            noButton { it.dismiss() }
                            yesButton(R.string.text_get_permission) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.fromParts("package", context.packageName, null)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                mActivity.startActivity(intent)
                            }
                        }.show()
                    } else {
                        context.toast("你取消了授权")
                    }
                }
            }
        }

        override fun filterKeys(): Array<String> = arrayOf(
            FULL_SCREEN_KEY,
            PERMISSION_REQUEST_KEY
        )
    }

    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {
        when (eventCode) {
            OnPlayerEventListener.PLAYER_EVENT_ON_STATUS_CHANGE -> {
                val stateCode = bundle!!.getInt(EventKey.INT_DATA)
                mStateButton.isSelected = stateCode == IPlayer.STATE_PAUSED
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET -> {
                val dataSource = bundle!!.getSerializable(EventKey.SERIALIZABLE_DATA) as DataSource
                groupValue.putObject(DATA_SOURCE_KEY, dataSource)
                bind.title.text = dataSource.title
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_START -> {
                val dataSource = groupValue.get<DataSource>(DATA_SOURCE_KEY)
                if (playerStateGetter?.currentPosition == 0) {
                    viewModel?.getVideoRecor(dataSource.data)
                }
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE -> { // 播放完成
                val playNext = mActivity.defaultSharedPreferences.getBoolean(
                    mActivity.getString(R.string.key_auto_play_next),
                    true
                )
                if (playNext) {
                    mNextJIButton.callOnClick()
                }
            }
        }
    }

    override fun onCreateCoverView(context: Context?): View {
        bind = LayoutCoverContralBinding.inflate(LayoutInflater.from(context)).apply {
            mSeekBar = playerControlSeekBar
            mProgressTimeText = currentTime
            mDurationTimeText = totalTime
            mTopLayout = topLayout
            mStateButton = stateButton
            mNextJIButton = nextJi
            mTimeTextView = time
            mBottomProgressBar = bottomProgressBar
            mControlView = controlView
            mReturnToLastWatchView = returnToLastWatchView
            onClick = this@ControlCover
        }
        initView()
        return bind.root
    }

    private fun initView() {
        mSeekBar.setOnSeekBarChangeListener(this)
    }

    override fun onReceiverBind() {
        super.onReceiverBind()
        groupValue.registerOnGroupValueUpdateListener(onGroupValueUpdateListener)
        viewModel?.videoRecordLiveData?.observe(mActivity) {
            val text = mActivity.getString(
                R.string.video_record_text,
                TimeUtil.getTimeSmartFormat(it.progress.toLong())
            )
            bind.lastWatchText.text = text

            mBundle.putInt(EventKey.INT_DATA, it.progress)
            bind.returnToLastWatchText.setOnClickListener {
                mHandler.removeMessages(HIDE_VIDEORECORD_VIEW_EVENT) // 移除延时自动隐藏
                mHandler.sendEmptyMessage(HIDE_VIDEORECORD_VIEW_EVENT) // 立即隐藏消息
                requestSeek(mBundle)
            }

            mReturnToLastWatchView.slideIn(250)
            mHandler.sendEmptyMessageDelayed(HIDE_VIDEORECORD_VIEW_EVENT, HIDE_DELAY)
        }
    }

    override fun onClick(view: View?) {
        view ?: return
        //重新延迟隐藏
        mHandler.removeMessages(HIDE_VIEV_EVENT)
        mHandler.sendEmptyMessageDelayed(HIDE_VIEV_EVENT, HIDE_DELAY)

        when (view.id) {
            R.id.state_button -> if (view.isSelected) requestResume(null) else requestPause(null)

            R.id.full_screen_button -> requestFullScreen(true)

            R.id.player_back -> requestFullScreen(false)

            R.id.setting -> {
                val currSpeed = groupValue.getFloat(SPEED_CONTROL_KEY, 1.0f)

                val settingBottomSheet = SettingBottomSheet.newInstance(currSpeed)
                settingBottomSheet.setItemClickListener {
                    settingBottomSheet.dismiss()
                    settingClickEvent(it)
                }
                settingBottomSheet.show(
                    mActivity.supportFragmentManager,
                    settingBottomSheet::class.java.simpleName
                )
            }

            R.id.hide -> {
                mHandler.removeMessages(HIDE_VIDEORECORD_VIEW_EVENT)
                mHandler.sendEmptyMessage(HIDE_VIDEORECORD_VIEW_EVENT)
            }

            R.id.next_ji -> {
                viewModel?.nextJi()
            }

        }
    }

    private fun settingClickEvent(posistion: Int) {
        when (posistion) {
            REPLAY_ITEM -> notifyReceiverEvent(REFRESH_VIDEO_CODE, null)

            SPEED_ITEM -> {
                val speedBottomSheet = SpeedBottomSheet.newInstence()
                speedBottomSheet.itemClickListener = {
                    speedBottomSheet.dismiss()
                    mBundle.putFloat(SPEED_CONTROL_KEY, it)
                    notifyReceiverEvent(SPEED_CONTROL_CODE, mBundle)
                }
                speedBottomSheet.show(
                    mActivity.supportFragmentManager,
                    speedBottomSheet::class.java.simpleName
                )
            }

            DOWNLOAD_ITEM -> {
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        mActivity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    viewModel?.downLoadVideo()
                }
            }

            DANMU_ITEM -> notifyReceiverPrivateEvent(
                DanmuCover::class.java.simpleName,
                SHOW_DANMU_SETTING_CODE,
                null
            )
        }
    }


    private fun requestFullScreen(isFullScreen: Boolean) {
        mBundle.clear()
        mBundle.putBoolean(FULL_SCREEN_KEY, isFullScreen)
        notifyReceiverEvent(FULL_SCREEN_CODE, mBundle)
    }

    private fun requestShowStateBar(isShow: Boolean) {
        mBundle.clear()
        mBundle.putBoolean(STATE_BAR_KEY, isShow)
        notifyReceiverEvent(STATE_BAR_CODE, mBundle)
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        seekBar?.let {
            if (fromUser) {
                updataTimeText(progress, seekBar.max)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.let {
            mBundle.clear()
            mBundle.putInt(EventKey.INT_DATA, it.progress)
            requestSeek(mBundle)
        }
    }

    /**
     * 单击事件, 隐藏/显示cover, 在范围时间内没有触发双击事件, 将cover
     *
     */
    override fun onSingleTapUp(event: MotionEvent?) {
        mHandler.removeMessages(HIDE_VIEV_EVENT)
        mHandler.removeMessages(SINGLE_TAP_EVENT)
        mHandler.sendEmptyMessageDelayed(SINGLE_TAP_EVENT, SINGLE_TAP_EVENT_DELAY)
    }

    /**
     * 双击事件为暂停/继续播放。
     * 双击事件发生单击事件onSingleTapUp()也会响应(在双击事件之前);
     * ,不希望单击事件(隐藏/显示cover)也响应, 所以要移除单击事件
     *
     */
    override fun onDoubleTap(event: MotionEvent?) {
        //移除单击事件
        mHandler.removeMessages(SINGLE_TAP_EVENT)

        playerStateGetter?.let {
            if (it.state == IPlayer.STATE_PAUSED) {
                requestResume(null)
                //ControlView可见时stateBar已在显示状态
                if (!mControlView.isVisible) {
                    requestShowStateBar(false)
                }
            } else if (it.state == IPlayer.STATE_STARTED) {
                requestPause(null)
                if (!mControlView.isVisible) {
                    requestShowStateBar(true)
                }
            }
        }
    }


    override fun onTimerUpdate(curr: Int, duration: Int, bufferPercentage: Int) {
        val seekBuffer = bufferPercentage * 1.0f / 100 * duration
        updateSeekBar(curr, duration, seekBuffer)
        updataTimeText(curr, duration)
    }

    private fun updateSeekBar(curr: Int, duration: Int, seekBuffer: Float?) {
        with(mBottomProgressBar) {
            max = duration
            progress = curr
            seekBuffer?.let { secondaryProgress = it.toInt() }
        }
        with(mSeekBar) {
            progress = curr
            max = duration
            seekBuffer?.let { secondaryProgress = it.toInt() }
        }
    }

    private fun updataTimeText(curr: Int, duration: Int) {
        mProgressTimeText.text = TimeUtil.getTimeSmartFormat(curr.toLong())
        mDurationTimeText.text = TimeUtil.getTimeSmartFormat(duration.toLong())
    }

    override fun onReceiverEvent(eventCode: Int, bundle: Bundle?) {

    }

    override fun onPrivateEvent(eventCode: Int, bundle: Bundle?): Bundle? {
        if (eventCode == UPDATE_SEEK_CODE) {
            val curr = bundle!!.getInt(UPDATE_SEEK_KEY)
            updateSeekBar(curr, playerStateGetter!!.duration, null)
            updataTimeText(curr, playerStateGetter!!.duration)
        }
        return bundle
    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {

    }

    override fun onReceiverUnBind() {
        bind.unbind()
        mHandler.removeCallbacksAndMessages(null)
        groupValue.unregisterOnGroupValueUpdateListener(onGroupValueUpdateListener)
        super.onReceiverUnBind()
    }

    override fun getCoverLevel(): Int {
        return levelLow(0)
    }

    fun showSettingBottomSheet() {
        bind.setting.callOnClick()
    }
}