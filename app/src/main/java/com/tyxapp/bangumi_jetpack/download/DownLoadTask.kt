package com.tyxapp.bangumi_jetpack.download

import android.content.Intent
import android.os.AsyncTask
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.data.DownloadProgress
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.db.DownLoadInfoDao
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

typealias OnProgressUpdateListener = (DownloadProgress) -> Unit

/**
 * 执行下载任务
 *
 */
class DownLoadTask(
    private var downLoadInfo: DownLoadInfo,
    private val mOnProgressUpdateListener: OnProgressUpdateListener?
) : AsyncTask<DownLoadInfo, Long, Int?>() {
    private val downLoadInfoDao: DownLoadInfoDao = AppDataBase.getInstance().downLoadInfoDao()
    private val downloadProgress = DownloadProgress(downLoadInfo.id, 0L, 0L)

    override fun doInBackground(vararg params: DownLoadInfo?): Int? {

        var inputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null

        val file = File(downLoadInfo.filePath)
        var progress = if (file.exists()) { // 获取文件长度
            file.length()
        } else {
            file.createNewFile()
            0L
        }
        var total = 0L

        try {
            val request = Request.Builder()
                .url(downLoadInfo.videoUrl)
                .addHeader("RANGE", "bytes=$progress-")
                .build()
            val responseBody = OkhttpUtil.getResponseBody(request)

            total = if (downLoadInfo.total == 0L) {
                responseBody.contentLength()
            } else {
                downLoadInfo.total
            }

            publishProgress(progress, total)
            updateState(DownLoadState.DOWNLOADING)
            updateDb(progress, total)

            inputStream = responseBody.byteStream()
            fileOutputStream = FileOutputStream(file, true)
            val byteArray = ByteArray(1024)
            var len: Int
            while (true) {
                len = inputStream.read(byteArray)
                if (len < 0) break
                fileOutputStream.write(byteArray, 0, len)
                progress += len
                publishProgress(progress, total)
            }

            updateState(DownLoadState.FINISH)
            return downLoadInfo.id
        } catch (e: Exception) {
            updateState(DownLoadState.FAILD)
            return null
        } finally {
            inputStream?.close()
            fileOutputStream?.close()
            updateDb(progress, total)
        }
    }

    override fun onProgressUpdate(vararg values: Long?) {
        mOnProgressUpdateListener?.let {
            downloadProgress.progress = values[0]!!
            downloadProgress.total = values[1]!!
            it.invoke(downloadProgress)
        }

    }

    override fun onCancelled() {
        updateState(DownLoadState.PUASE)
    }

    private fun updateState(state: DownLoadState) {
        downLoadInfo.state = state
        downLoadInfoDao.update(downLoadInfo)
        LocalBroadcastManager.getInstance(BangumiApp.getContext())
            .sendBroadcast(Intent().apply {
                action = DOWNLOAD_RECEIVER_ACTION_NAME
                putExtra(DOWNLOAD_STATE_ID_EXTRA_KEY, downLoadInfo.id)
                putExtra(DOWNLOAD_STATE_EXTRA_KEY, state.name)
            })
    }

    /**
     * 更新数据库进度
     *
     */
    private fun updateDb(progress: Long, total: Long) {
        //获取最新状态
        val newDownLoadInfo = downLoadInfoDao.getDownLoadInfoBy(downLoadInfo.id)!!
        downLoadInfo = newDownLoadInfo
        downLoadInfo.currentPosition = progress
        downLoadInfo.total = total
        downLoadInfoDao.update(downLoadInfo)
    }

}