package com.tyxapp.bangumi_jetpack.player.cover

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.kk.taurus.playerbase.assist.InterKey
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.receiver.BaseCover
import com.kk.taurus.playerbase.utils.NetworkUtils
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.player.ERROR_COVER_VISIBLE_CODE
import com.tyxapp.bangumi_jetpack.player.ERROR_COVER_VISIBLE_KEY
import com.tyxapp.bangumi_jetpack.utilities.isGone
import org.jetbrains.anko.toast

private enum class ErrorEvent {
    MOBILE_NETWORK, LOADED_ERROR
}

class ErrorCover(
    context: Context
) : BaseCover(context) {
    private lateinit var mRetryButton: Button
    private lateinit var mErrorText: TextView

    private var currentErrorEvent: ErrorEvent? = null
    private val mBundle = Bundle()
    
    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {
        when(eventCode) {
            OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET -> {
                val playInMobileNet = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    context.getString(R.string.key_play_moibleState),
                    false
                )
                setCoverVisibility(false)
                if (isMobile() && !playInMobileNet) {
                    setCoverState(ErrorEvent.MOBILE_NETWORK)
                }
            }

            OnPlayerEventListener.PLAYER_EVENT_ON_PREPARED -> {
                if (view.isVisible && currentErrorEvent == ErrorEvent.MOBILE_NETWORK) {
                    requestStop(null)
                }
            }
        }
    }

    override fun onReceiverEvent(eventCode: Int, bundle: Bundle?) {

    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {
        setCoverState(ErrorEvent.LOADED_ERROR)
    }

    override fun onProducerData(key: String?, data: Any?) {
        if (key.equals(InterKey.KEY_NETWORK_STATE)) {
            if (isMobile()) {
                context.toast("您正在使用移动网络喔~")
            }
        }
    }

    override fun onCreateCoverView(context: Context?): View {
        return LayoutInflater.from(context).inflate(
            R.layout.layout_error_cover, null, false
        )
    }

    override fun onReceiverBind() {
        super.onReceiverBind()
        initView()
    }

    private fun initView() {
        mRetryButton = view.findViewById(R.id.retryButton)
        mErrorText = view.findViewById(R.id.error_text)
        mRetryButton.setOnClickListener {
            requestRetry(null)
            setCoverVisibility(false)
        }
    }

    private fun setCoverState(errorEvent: ErrorEvent) {
        currentErrorEvent = errorEvent
        when(errorEvent) {
            ErrorEvent.MOBILE_NETWORK -> {
                mErrorText.setText(R.string.text_use_mobile)
                mRetryButton.setText(R.string.text_continue)
            }
            
            ErrorEvent.LOADED_ERROR -> {
                mErrorText.setText(R.string.text_video_loading_error)
                mRetryButton.setText(R.string.button_text_retry)
            }
        }
        setCoverVisibility(true)
    }

    private fun isMobile() = NetworkUtils.isMobile(NetworkUtils.getNetworkState(context))

    override fun getCoverLevel(): Int {
        return levelHigh(0)
    }

    private fun setCoverVisibility(isVisible: Boolean) {
        view.isGone(!isVisible)
        mBundle.putBoolean(ERROR_COVER_VISIBLE_KEY, isVisible)
        notifyReceiverEvent(ERROR_COVER_VISIBLE_CODE, mBundle)
    }

}