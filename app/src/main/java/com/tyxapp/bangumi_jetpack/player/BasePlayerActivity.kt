package com.tyxapp.bangumi_jetpack.player

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.receiver.ReceiverGroup
import com.kk.taurus.playerbase.widget.BaseVideoView

abstract class BasePlayerActivity : AppCompatActivity() {
    var isUserPause = false
    lateinit var mVideoView: BaseVideoView
    lateinit var mReceiver: ReceiverGroup
    var currentPosition = 0 //计录停止时的播放进度
    var isActive = false //当前Activity是否可见

    fun setVideoView(videoView: BaseVideoView) {
        mVideoView = videoView
        mVideoView.setOnPlayerEventListener { eventCode, _ ->
            when (eventCode) {
                OnPlayerEventListener.PLAYER_EVENT_ON_START -> {
                    if (!isActive) stopPlayer()
                    val speed = mReceiver.groupValue.getFloat(SPEED_CONTROL_KEY, 1.0f)
                    mVideoView.setSpeed(speed)
                }
            }
        }
    }

    /**
     * stop后VideoView进度会清0, 要计录下停止钱进度
     *
     */
    fun stopPlayer() {
        currentPosition = mVideoView.currentPosition
        mVideoView.stop()
    }

    fun setReceiverGroup(receiverGroup: ReceiverGroup) {
        mReceiver = receiverGroup
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (mVideoView.state == IPlayer.STATE_STOPPED) {
            mVideoView.rePlay(currentPosition)
        } else if (mVideoView.isInPlaybackState) {
            if (!isUserPause) {
                mVideoView.resume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isActive = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (mVideoView.isInPlaybackState) {
            if (mVideoView.isPlaying) {
                mVideoView.pause()
                isUserPause = false
            }
        }
    }

    override fun onDestroy() {
        mVideoView.stopPlayback()
        mReceiver.clearReceivers()
        super.onDestroy()
    }

}