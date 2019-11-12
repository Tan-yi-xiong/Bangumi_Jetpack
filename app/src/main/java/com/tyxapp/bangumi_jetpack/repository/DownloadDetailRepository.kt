package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.db.DownLoadInfoDao
import com.tyxapp.bangumi_jetpack.download.DownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DownloadDetailRepository private constructor(
    private val downLoadInfoDao: DownLoadInfoDao
) {

    fun getBangumiDownLoadVideo(bangumiId: String, bangumiSource: String) =
        downLoadInfoDao.getBangumiDownLoadVideo(bangumiId, bangumiSource)

    suspend fun getDownloadInfoBy(id: Int) = withContext(Dispatchers.IO) {
            downLoadInfoDao.getDownLoadInfoBy(id)
        }

    suspend fun removeTask(downLoadInfo: DownLoadInfo) = withContext(Dispatchers.IO) {
        if (downLoadInfo.state == DownLoadState.DOWNLOADING) {
            DownloadManager.puase(downLoadInfo.id)
        }
        File(downLoadInfo.filePath).delete()
        downLoadInfoDao.delete(downLoadInfo)
    }

    companion object {
        private var INSTANCE: DownloadDetailRepository? = null

        fun getInstance(): DownloadDetailRepository {
            INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = DownloadDetailRepository(AppDataBase.getInstance().downLoadInfoDao())
                }
            }
            return INSTANCE!!
        }
    }
}