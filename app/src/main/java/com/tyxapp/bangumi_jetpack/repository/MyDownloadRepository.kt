package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.db.BangumiDetailDao
import com.tyxapp.bangumi_jetpack.data.db.DownLoadInfoDao
import com.tyxapp.bangumi_jetpack.download.DownloadManager
import java.io.File

class MyDownloadRepository private constructor(
    private val bangumiDetailDao: BangumiDetailDao,
    private val downLoadInfoDao: DownLoadInfoDao
) {
    /**
     * 获取有下载的番剧信息
     *
     */
    fun getDownloadBangumi() = bangumiDetailDao.getDownloadBagnumi()

    suspend fun removeDownloadBangmi(bangumiDetail: BangumiDetail) {
        bangumiDetail.isDownLoad = false
        bangumiDetailDao.update(bangumiDetail)
        downLoadInfoDao.getBangumiDownLoadVideolist(bangumiDetail.id, bangumiDetail.source.name).forEach {
            if (it.state == DownLoadState.DOWNLOADING) {
                DownloadManager.puase(it.id)
            }
            File(it.filePath).delete()
            downLoadInfoDao.delete(it)
        }
    }

    companion object {
        private var INSTANCE: MyDownloadRepository? = null

        fun getInstance(): MyDownloadRepository {
            INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = MyDownloadRepository(
                        AppDataBase.getInstance().bangumiDetailDao(),
                        AppDataBase.getInstance().downLoadInfoDao()
                    )
                }
            }
            return INSTANCE!!
        }
    }
}