package com.tyxapp.bangumi_jetpack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownLoadInfo(
    val videoUrl: String,
    val bangumiId: String,
    val bangumiSource: BangumiSource,
    var state: DownLoadState = DownLoadState.WAIT,
    var currentPosition: Long = 0L,
    var total: Long = 0L,
    val name: String = "",
    val filePath: String = "",

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)

enum class DownLoadState {
    DOWNLOADING, PUASE, FAILD, WAIT, FINISH
}