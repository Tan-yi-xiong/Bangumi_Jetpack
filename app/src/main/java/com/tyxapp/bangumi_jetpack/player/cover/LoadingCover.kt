package com.tyxapp.bangumi_jetpack.player.cover

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.receiver.BaseCover
import com.kk.taurus.playerbase.receiver.PlayerStateGetter
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import com.tyxapp.bangumi_jetpack.utilities.isGone
import com.tyxapp.bangumi_jetpack.views.PlayerLoadingView


class LoadingCover(
    context: Context
) : BaseCover(context) {

    private val loadingStateCodes = intArrayOf(
        OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_START,
        OnPlayerEventListener.PLAYER_EVENT_ON_DATA_SOURCE_SET,
        OnPlayerEventListener.PLAYER_EVENT_ON_PROVIDER_DATA_START,
        OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_TO
    )

    private val noLoadingCodes = intArrayOf(
        OnPlayerEventListener.PLAYER_EVENT_ON_VIDEO_RENDER_START,
        OnPlayerEventListener.PLAYER_EVENT_ON_BUFFERING_END,
        OnPlayerEventListener.PLAYER_EVENT_ON_STOP,
        OnPlayerEventListener.PLAYER_EVENT_ON_PROVIDER_DATA_ERROR
        //OnPlayerEventListener.PLAYER_EVENT_ON_SEEK_COMPLETE
    )


    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {
        when (eventCode) {
            in loadingStateCodes -> setLoadingState(true)
            in noLoadingCodes -> setLoadingState(false)
        }
    }

    private fun setLoadingState(isShow: Boolean) {
        if (view.isVisible != isShow) {
            view.isGone(!isShow)
        }
    }

    override fun onReceiverEvent(eventCode: Int, bundle: Bundle?) {
    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {
    }

    override fun onCreateCoverView(context: Context?): View {
        return PlayerLoadingView(context).apply {
            isGone(true)
        }
    }

    override fun getCoverLevel(): Int {
        return levelLow(1)
    }
}