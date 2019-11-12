package com.tyxapp.bangumi_jetpack.player

import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler
import com.kk.taurus.playerbase.entity.DataSource
import com.kk.taurus.playerbase.receiver.ReceiverGroup
import com.kk.taurus.playerbase.widget.BaseVideoView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.player.cover.*

class LocalPlayerActivity : BasePlayerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_loacl_player)
        setVideoView(findViewById(R.id.videoView))
        setReceiverGroup(ReceiverGroup().apply {
            addReceiver(
                ControlCover::class.java.simpleName,
                ControlCover(this@LocalPlayerActivity, null)
            )
            addReceiver(LoadingCover::class.java.simpleName, LoadingCover(this@LocalPlayerActivity))
            addReceiver(GestureCover::class.java.simpleName, GestureCover(this@LocalPlayerActivity))
            addReceiver(ErrorCover::class.java.simpleName, ErrorCover(this@LocalPlayerActivity))
        })
        mVideoView.setReceiverGroup(mReceiver)
        mVideoView.setEventHandler(onVideoViewEventHandler)
        mReceiver.groupValue.putBoolean(FULL_SCREEN_KEY, true, true)
        val url = intent.getStringExtra(LOCAL_VIDEO_URL_KEY)
        mVideoView.setDataSource(DataSource(url))
        mVideoView.post { mVideoView.start() }
        initSensor()
    }

    private fun initSensor() {
        val sensorManager = getSystemService<SensorManager>()!!
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val mSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent?) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

    }

    private val onVideoViewEventHandler = object : OnVideoViewEventHandler() {

        override fun requestPause(videoView: BaseVideoView?, bundle: Bundle?) {
            super.requestPause(videoView, bundle)
            isUserPause = true
        }

        override fun requestResume(videoView: BaseVideoView?, bundle: Bundle?) {
            super.requestResume(videoView, bundle)
            isUserPause = false
        }

        override fun onAssistHandle(assist: BaseVideoView?, eventCode: Int, bundle: Bundle?) {
            super.onAssistHandle(assist, eventCode, bundle)
            when (eventCode) {
                FULL_SCREEN_CODE -> finish()

                REFRESH_VIDEO_CODE -> mVideoView.rePlay(mVideoView.currentPosition) //刷新视频

                SPEED_CONTROL_CODE -> {//视频变速
                    val speed = bundle!!.getFloat(SPEED_CONTROL_KEY)
                    assist?.setSpeed(speed)
                    mReceiver.groupValue.putFloat(SPEED_CONTROL_KEY, speed)
                }

            }
        }
    }
}
