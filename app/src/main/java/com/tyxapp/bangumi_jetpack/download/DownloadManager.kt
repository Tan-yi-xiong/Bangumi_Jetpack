package com.tyxapp.bangumi_jetpack.download

import android.annotation.SuppressLint
import android.content.*
import android.os.Environment
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.util.concurrent.locks.ReentrantLock

/**
 * 下载管理器, 负责控制服务要执行的任务
 *
 */
@SuppressLint("StaticFieldLeak")
object DownloadManager {

    private val lock = ReentrantLock()
    private val context = BangumiApp.getContext()
    private val downLoadInfoDao = AppDataBase.getInstance().downLoadInfoDao()
    private var mDownloadServer: DownloadVideoServer? = null
    private val mCurrentExecuteTasks = ArrayList<DownLoadInfo>() // 正在执行的任务
    private val mOnProgressUpdateListeners = ArrayList<OnProgressUpdateListener>()
    private var maxDownloadTask: Int = 3
    private val mConnect = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mDownloadServer = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mDownloadServer = (service as DownloadVideoServer.DownloadBinder).getDownloadServer()
            mDownloadServer!!.onProgressUpdateListener = { downloadProgress ->
                mOnProgressUpdateListeners.forEach { it.invoke(downloadProgress) }
            }
            mCurrentExecuteTasks.forEach { mDownloadServer?.startTask(it) }
        }

    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, receiveintent: Intent?) {
            receiveintent?.let { intent ->
                if (intent.action == DOWNLOAD_RECEIVER_ACTION_NAME) {
                    val downloadState =
                        DownLoadState.valueOf(intent.getStringExtra(DOWNLOAD_STATE_EXTRA_KEY))
                    val id = intent.getIntExtra(DOWNLOAD_STATE_ID_EXTRA_KEY, 0)

                    when(downloadState) {
                        DownLoadState.FINISH, DownLoadState.FAILD, DownLoadState.PUASE -> {
                            val downLoadInfo = mCurrentExecuteTasks.find { it.id == id }
                            downLoadInfo?.let { mCurrentExecuteTasks.remove(it) }
                            findNextTask()
                        }
                    }
                }
            }
        }
    }

    init {
        // 获取用户设定最大下载数量
        maxDownloadTask =
            context.defaultSharedPreferences.getInt(context.getString(R.string.key_max_download), 3)

        LocalBroadcastManager.getInstance(BangumiApp.getContext())
            .registerReceiver(mReceiver, IntentFilter(DOWNLOAD_RECEIVER_ACTION_NAME))
    }

    /**
     * 根据参数创建一个下载信息, 并返回
     * @return 已存在数据库返回null
     */
    fun creatDownloadInfo(
        bangumiId: String,
        bangumiSource: BangumiSource,
        videoUrl: String,
        fileName: String
    ): DownLoadInfo? {
        val id = downLoadInfoDao.getDownLoadInfoBy(videoUrl)
        return if (id != null) { // 已存在
            null
        } else {
            val downLoadInfo = DownLoadInfo(
                bangumiId = bangumiId,
                videoUrl = videoUrl,
                name = fileName,
                bangumiSource = bangumiSource,
                filePath = mkFilePath(bangumiSource, bangumiId, fileName)
            )
            downLoadInfo
        }
    }

    private fun mkFilePath(
        bangumiSource: BangumiSource,
        bangumiId: String,
        fileName: String
    ): String {
        //文件父目录不存在的话先创建
        val dirPath = buildString {
            append(Environment.getExternalStorageDirectory().absolutePath)
            append("/Bangumi/${bangumiSource.name}/$bangumiId")
        }
        File(dirPath).also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }

        return "$dirPath/$fileName"
    }

    /**
     * 立即处理下载
     *
     */
    fun startNow(id: Int) {
        lock.lock()
        if (mCurrentExecuteTasks.size == maxDownloadTask) { // 正在执行的任务满了就移除其中一个
            val removeDownLoadInfo = mCurrentExecuteTasks[mCurrentExecuteTasks.size - 1]
            mDownloadServer?.puase(removeDownLoadInfo.id)
            mCurrentExecuteTasks.remove(removeDownLoadInfo)
        }
        lock.unlock()
        val downLoadInfo = downLoadInfoDao.getDownLoadInfoBy(id)
        downLoadInfo?.let {
            //开始任务
            mCurrentExecuteTasks.add(it)
            mDownloadServer?.startTask(it)
        }
        checkBindServer()
    }

    /**
     * 如果正在执行的任务数量大于最大下载量, 则会把任务设为等待
     *
     */
    fun start(downLoadInfo: DownLoadInfo) {
        // 任务不在下载列表添加到数据库
        val hasAdd = downLoadInfoDao.getDownLoadInfoBy(downLoadInfo.videoUrl)
        if (hasAdd == null) {
            val id = downLoadInfoDao.insert(downLoadInfo)
            downLoadInfo.id = id.toInt()
        }

        // 已在下载
        if (mCurrentExecuteTasks.find { downLoadInfo.id == it.id } != null) {
            return
        }

        lock.lock()
        if (mCurrentExecuteTasks.size == maxDownloadTask) {
            downLoadInfo.state = DownLoadState.WAIT
            downLoadInfoDao.update(downLoadInfo)
        } else {
            mCurrentExecuteTasks.add(downLoadInfo)
            mDownloadServer?.startTask(downLoadInfo)
        }
        lock.unlock()
        checkBindServer()
    }

    /**
     * 数据库寻找下一个任务
     *
     */
    private fun findNextTask() {
        // 获取等待的任务
        val waitDownLoadInfo =
            downLoadInfoDao.getWaitDownLoad(1).takeIf { it.isNotEmpty() }?.get(0)

        if (waitDownLoadInfo != null) {
            if (mCurrentExecuteTasks.size < maxDownloadTask) {
                mCurrentExecuteTasks.add(waitDownLoadInfo)
                mDownloadServer?.startTask(waitDownLoadInfo)
            }
        }
    }

    /**
     * 根据数据库id暂停任务
     * @param dbId 数据库id
     * @return 暂停是否成功
     */
    fun puase(dbId: Int): Boolean {
        val downLoadInfo = mCurrentExecuteTasks.find { it.id == dbId }
        if (mDownloadServer == null || downLoadInfo === null) return false
        mDownloadServer!!.puase(dbId)
        mCurrentExecuteTasks.remove(downLoadInfo)
        findNextTask()
        return true
    }

    private fun checkBindServer() {
        if (mDownloadServer == null) {
            context.bindService(
                Intent(context, DownloadVideoServer::class.java),
                mConnect, Context.BIND_AUTO_CREATE
            )
        }
    }

    fun downLoadApk() {

    }

    fun addOnProgressUpdateListener(onProgressUpdateListener: OnProgressUpdateListener) {
        mOnProgressUpdateListeners.add(onProgressUpdateListener)
    }

    fun removeOnProgressUpdateListener(onProgressUpdateListener: OnProgressUpdateListener) {
        mOnProgressUpdateListeners.remove(onProgressUpdateListener)
    }

}