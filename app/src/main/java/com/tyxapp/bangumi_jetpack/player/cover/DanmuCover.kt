package com.tyxapp.bangumi_jetpack.player.cover

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewStub
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.kk.taurus.playerbase.event.EventKey
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.receiver.BaseCover
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.player.PlayerViewModel
import com.tyxapp.bangumi_jetpack.player.SHOW_DANMU_SETTING_CODE
import com.tyxapp.bangumi_jetpack.player.bottomsheet.DanmuSettingBottomSheet
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast


class DanmuCover(
    private val mActivity: AppCompatActivity,
    private val viewModel: PlayerViewModel
) : BaseCover(mActivity) {

    private lateinit var viewStub: ViewStub
    private lateinit var mDanmakuView: DanmakuView
    private lateinit var mDanmakuContext: DanmakuContext
    private lateinit var mBaseDanmakuParser: BaseDanmakuParser

    private val mListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (!::mDanmakuContext.isInitialized) return@OnSharedPreferenceChangeListener
            if (key == context.getString(R.string.key_danmaku_textsize)) {
                val scaleTextSize = sharedPreferences.getInt(
                    context.getString(R.string.key_danmaku_textsize),
                    70
                )
                mDanmakuContext.setScaleTextSize(scaleTextSize / 100.0f)
            } else if (key == context.getString(R.string.key_danmaku_maxRaw)) {
                val maxRaw = mActivity.defaultSharedPreferences.getInt(
                    context.getString(R.string.key_danmaku_maxRaw),
                    3
                )
                mDanmakuContext.setMaximumLines(hashMapOf(BaseDanmaku.TYPE_SCROLL_RL to maxRaw))
            }
        }

    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {
        when (eventCode) {
            OnPlayerEventListener.PLAYER_EVENT_ON_PAUSE -> {
                if (checkDanmakuViewHasInit()) {
                    mDanmakuView.pause()
                }
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_START -> {
                if (checkDanmakuViewHasInit()) {
                    mDanmakuView.start()
                }
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_RESUME -> {
                if (checkDanmakuViewHasInit()) {
                    mDanmakuView.resume()
                }
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO -> {
                if (checkDanmakuViewHasInit()) {
                    val position = bundle!!.getInt(EventKey.INT_DATA).toLong()
                    mDanmakuView.seekTo(if (position < 0) 0 else position)
                    mDanmakuView.pause()
                }
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE -> {
                if (checkDanmakuViewHasInit()) {
                    val isPlay = playerStateGetter?.state == IPlayer.STATE_STARTED
                    if (mDanmakuView.isPaused && isPlay) {
                        mDanmakuView.resume()
                    }
                }
            }
        }
    }

    private fun checkDanmakuViewHasInit(): Boolean =
        ::mDanmakuView.isInitialized && mDanmakuView.isPrepared

    override fun onReceiverEvent(eventCode: Int, bundle: Bundle?) {
    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {
    }

    override fun onCreateCoverView(context: Context?): View {
        val view = LayoutInflater.from(context).inflate(
            R.layout.layout_cover_danmu,
            null, false
        )
        viewStub = view.findViewById(R.id.viewStub)
        return view
    }

    override fun onReceiverBind() {
        super.onReceiverBind()
        mActivity.defaultSharedPreferences.registerOnSharedPreferenceChangeListener(mListener)
        viewModel.baseDanmakuParserLiveData.observe(mActivity) { danmakuParser ->
            if (!::mDanmakuView.isInitialized) {
                initView()
            }
            mDanmakuView.doOnPrepared {
                val userSettingShow = mActivity.defaultSharedPreferences.getBoolean(
                    context.getString(R.string.key_danmaku_auto_show),
                    true
                )

                if (!userSettingShow && mDanmakuView.isShown) {
                    mDanmakuView.hide()
                }
            }

            if (mDanmakuView.isPrepared && ::mBaseDanmakuParser.isInitialized) {
                mBaseDanmakuParser.release()
                mDanmakuView.release()
            }

            mBaseDanmakuParser = danmakuParser
            mDanmakuView.prepare(danmakuParser, mDanmakuContext)
        }
    }

    @SuppressLint("UseSparseArrays")
    private fun initView() {
        mDanmakuContext = DanmakuContext.create()
        mDanmakuView = viewStub.inflate().findViewById(R.id.danmakuView)

        val maxLine = mActivity.defaultSharedPreferences.getInt(
            context.getString(R.string.key_danmaku_maxRaw),
            3
        )

        val textSize = mActivity.defaultSharedPreferences.getInt(
            context.getString(R.string.key_danmaku_textsize),
            70
        )

        val maxLInesPair = hashMapOf(
            BaseDanmaku.TYPE_SCROLL_RL to maxLine
        )

        val overlappingEnablePair = hashMapOf(
            BaseDanmaku.TYPE_SCROLL_RL to true,
            BaseDanmaku.TYPE_FIX_TOP to true
        )

        //设置一些相关的配置
        mDanmakuContext
            .setDuplicateMergingEnabled(true)
            //是否重复合并
            .setScrollSpeedFactor(1.2f)
            //设置文字的比例
            .setScaleTextSize(textSize / 100.0f)
            //设置显示最大行数
            .setMaximumLines(maxLInesPair)
            //设置防，null代表可以重叠
            .preventOverlapping(overlappingEnablePair)
    }

    override fun onPrivateEvent(eventCode: Int, bundle: Bundle?): Bundle? {
        if (eventCode == SHOW_DANMU_SETTING_CODE) {
            if (!::mDanmakuView.isInitialized) {
                context.toast(R.string.text_no_danmu)
                return bundle
            }

            DanmuSettingBottomSheet.getInstance(mDanmakuView.isShown).also {
                it.setOnCheckedChangeListener { isCheck ->
                    if (isCheck) mDanmakuView.show() else mDanmakuView.hide()
                }

                it.show(
                    mActivity.supportFragmentManager,
                    DanmuSettingBottomSheet::class.java.simpleName
                )
            }
        }
        return super.onPrivateEvent(eventCode, bundle)
    }


    override fun onReceiverUnBind() {
        if (checkDanmakuViewHasInit()) {
            mDanmakuView.release()
        }
        mActivity.defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener)
        super.onReceiverUnBind()
    }

}

private inline fun DanmakuView.doOnPrepared(crossinline action: () -> Unit) {
    setCallback(object : DrawHandler.Callback {
        override fun drawingFinished() {

        }

        override fun danmakuShown(danmaku: BaseDanmaku?) {
        }

        override fun prepared() {
            action()
        }

        override fun updateTimer(timer: DanmakuTimer?) {
        }

    })
}