package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.db.BangumiDetailDao
import com.tyxapp.bangumi_jetpack.data.db.VideoRecordDao
import com.tyxapp.bangumi_jetpack.data.parsers.IPlayerVideoParser
import com.tyxapp.bangumi_jetpack.download.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast

class PlayerRepository(
    private val playerVideoParser: IPlayerVideoParser,
    private val bangumiDetailDao: BangumiDetailDao,
    private val videoRecordDao: VideoRecordDao
) {
    suspend fun getBangumiDetail(id: String): BangumiDetail {
        val netDetail = playerVideoParser.getBangumiDetail(id)
        val dbDetail = bangumiDetailDao.getBangumiDetail(id, netDetail.source.name)
        return if (dbDetail == null) {
            netDetail.apply {
                netDetail.lastWatchTime = System.currentTimeMillis()
                dbId = bangumiDetailDao.insert(netDetail)
            }
        } else {
            mergeBangumiDetail(netDetail, dbDetail).apply {
                lastWatchTime = System.currentTimeMillis() // 更新观看时间
                bangumiDetailDao.update(this)
            }
        }
    }

    private fun mergeBangumiDetail(
        netDetail: BangumiDetail,
        dbDetail: BangumiDetail
    ): BangumiDetail {
        return netDetail.apply {
            dbId = dbDetail.dbId
            isFollow = dbDetail.isFollow
            lastWatchTime = dbDetail.lastWatchTime
            lastWatchJi = dbDetail.lastWatchJi
            lastWatchLine = dbDetail.lastWatchLine
        }
    }

    suspend fun updateBangumiDetail(bangumiDetail: BangumiDetail) =
        bangumiDetailDao.update(bangumiDetail)

    suspend fun getlineWithJiItem(id: String) = playerVideoParser.getJiList(id)

    suspend fun getPlayerUrl(id: String, ji: Int, line: Int) =
        playerVideoParser.getPlayerUrl(id, ji, line)

    suspend fun getRecommendBangumis(id: String) = playerVideoParser.getRecommendBangumis(id)

    suspend fun getDanmakuParser(id: String, ji: Int) = playerVideoParser.getDanmakuParser(id, ji)

    suspend fun getVideoRecord(url: String) = videoRecordDao.getVideoRecord(url)

    /**
     *
     * @return 成功开始下载返回null, 否则返回失败信息
     */
    fun downLoadVideo(bangumiDetail: BangumiDetail, url: String, fileName: String = url): String? {

        if (url.contains(".m3u8")) return "暂时无法下载此视频"

        val downloadInfo =
            DownloadManager.creatDownloadInfo(
                bangumiDetail.id,
                bangumiDetail.source, url,
                "$fileName.mp4"
            )

        return if (downloadInfo == null) {
            "此视频已在下载列表"
        } else {
            DownloadManager.start(downloadInfo)
            null
        }
    }
}