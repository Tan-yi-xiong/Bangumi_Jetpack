package com.tyxapp.bangumi_jetpack.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.SparseArray
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.DownLoadState
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.START_DOWNLOAD

/**
 * 下载服务, 负责管理正在下载任务
 *
 */
class DownloadVideoServer : Service() {
    // 执行的任务列表
    private val tasks = SparseArray<DownLoadTask>()
    private val mReceiver = DownLoadStateReceiver()
    var onProgressUpdateListener: OnProgressUpdateListener? = null
    private val mNotificationManagerBuilder by lazy(LazyThreadSafetyMode.NONE) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(START_DOWNLOAD, true)
        }

        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在下载${tasks.size()}个视频")
            .setSmallIcon(R.drawable.ic_notifi_icon)
            .setWhen(System.currentTimeMillis())
            .setVibrate(longArrayOf(0L))
            .setSound(null)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "下载通知", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    enableLights(false)
                    enableVibration(false)
                    vibrationPattern = longArrayOf(0L)
                    setSound(null, null)
                }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mReceiver,
            IntentFilter(DOWNLOAD_RECEIVER_ACTION_NAME)
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return DownloadBinder()
    }

    fun startTask(downLoadInfo: DownLoadInfo) {
        val task = DownLoadTask(downLoadInfo, onProgressUpdateListener)
        addTask(downLoadInfo.id, task)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    fun puase(id: Int) {
        tasks[id]?.let {
            it.cancel(true) // 同时中断线程任务
            removeTask(id)
        }
    }

    private fun addTask(id: Int, task: DownLoadTask) {
        tasks.append(id, task)
        mNotificationManagerBuilder.setContentTitle("正在下载${tasks.size()}个视频")
        startForeground(FOREGROUND_ID, mNotificationManagerBuilder.build())
    }

    private fun removeTask(id: Int) {
        tasks.remove(id)
        mNotificationManagerBuilder.setContentTitle("正在下载${tasks.size()}个视频")
        if (tasks.size() == 0) {
            stopForeground(true)
        } else {
            startForeground(FOREGROUND_ID, mNotificationManagerBuilder.build())
        }

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    inner class DownloadBinder : Binder() {
        fun getDownloadServer(): DownloadVideoServer = this@DownloadVideoServer
    }

    private inner class DownLoadStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == DOWNLOAD_RECEIVER_ACTION_NAME) {
                    val downloadState =
                        DownLoadState.valueOf(it.getStringExtra(DOWNLOAD_STATE_EXTRA_KEY))


                    if (downloadState == DownLoadState.FINISH || downloadState == DownLoadState.FAILD) {
                        val id = it.getIntExtra(DOWNLOAD_STATE_ID_EXTRA_KEY, 0)
                        removeTask(id)
                    }
                }
            }
        }
    }
}
