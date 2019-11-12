package com.tyxapp.bangumi_jetpack.player

import com.kk.taurus.playerbase.entity.DataSource
import com.kk.taurus.playerbase.record.OnRecordCallBack
import com.tyxapp.bangumi_jetpack.data.VideoRecord
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

/**
 * playerBase框架保存视频播放进度回调
 *
 */
object RecordCallBack : OnRecordCallBack {
    private val videoRecordDao = AppDataBase.getInstance().videoRecordDao()

    override fun onResetRecord(dataSource: DataSource?): Int {
        return 0
    }

    override fun onSaveRecord(dataSource: DataSource?, record: Int): Int {
        if (dataSource == null || record <= 30 * 1000) return record
        CoroutineScope(Dispatchers.Main).launch {
            val videoRecord = videoRecordDao.getVideoRecord(dataSource.data)
            if (videoRecord == null) {
                videoRecordDao.insert(VideoRecord(dataSource.data, record))
            } else {
                videoRecord.progress = record
                videoRecordDao.update(videoRecord)
            }
        }
        return record
    }

    override fun onGetRecord(dataSource: DataSource?): Int {
        return 0
    }

    override fun onRemoveRecord(dataSource: DataSource?): Int {
        return 0
    }

    override fun onClearRecord() {

    }
}