package com.tyxapp.bangumi_jetpack

import android.app.Application
import android.content.Context
import com.kk.taurus.ijkplayer.IjkPlayer
import com.kk.taurus.playerbase.config.PlayerConfig
import com.kk.taurus.playerbase.config.PlayerLibrary
import com.kk.taurus.playerbase.record.PlayRecordManager
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.download.DownloadManager
import com.tyxapp.bangumi_jetpack.player.RecordCallBack
import com.tyxapp.bangumi_jetpack.utilities.PrefUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File

class BangumiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        initPlayer()
        initDownload()
    }

    /**
     * 检查下载文件是否有被用户删除, 有的话移除数据库
     *
     */
    private fun initDownload() {
        CoroutineScope(Dispatchers.Default).launch {
            val downLoadInfoDao = AppDataBase.getInstance().downLoadInfoDao()
            downLoadInfoDao.getAll().forEach {
                if (!File(it.filePath).exists()) {
                    downLoadInfoDao.delete(it)
                }

                if (it.state == DownLoadState.DOWNLOADING || it.state == DownLoadState.FAILD) {
                    it.state = DownLoadState.WAIT
                    downLoadInfoDao.update(it)
                }
            }

            // 是否设置为自动回复下载
            val autoResumeDownload = defaultSharedPreferences.getBoolean(
                getString(R.string.key_auto_resume_download),
                true
            )
            if (autoResumeDownload) {
                val maxDownloadCount = defaultSharedPreferences.getInt(getString(R.string.key_max_download), 3)
                downLoadInfoDao.getWaitDownLoad(maxDownloadCount).forEach {
                    DownloadManager.start(it)
                }
            }

            // 把没有下载任务的已出数据库
            val bangumiDetailDao = AppDataBase.getInstance().bangumiDetailDao()
            bangumiDetailDao.getDownloadBagnumiList().forEach {
                if (downLoadInfoDao.getBangumiDownLoadVideolist(it.id, it.source.name).isEmpty()) {
                    it.isDownLoad = false
                    bangumiDetailDao.update(it)
                }
            }
        }
    }

    /**
     * 初始化视频播放库
     *
     */
    private fun initPlayer() {
        PlayerConfig.setUseDefaultNetworkEventProducer(true)

        // 选择解码
        val decodePlanNameArray = resources.getStringArray(R.array.decoder_plan_value)
        when (PrefUtils.getDecodePlanName(decodePlanNameArray[0])) {
            decodePlanNameArray[0] -> IjkPlayer.init(this)
            decodePlanNameArray[1] -> PlayerLibrary.init(this)
        }

        //保存视频播放进度
        PlayerConfig.playRecord(true)
        PlayRecordManager.setRecordConfig(
            PlayRecordManager.RecordConfig.Builder()
                .setMaxRecordCount(10000)
                .setOnRecordCallBack(RecordCallBack)
                .build()
        )
    }

    companion object {
        private var appContext: Context? = null

        fun getContext() = appContext!!
    }
}