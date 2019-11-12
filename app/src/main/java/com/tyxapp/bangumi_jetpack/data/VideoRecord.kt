package com.tyxapp.bangumi_jetpack.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 视频进度记录数据类
 *
 */
@Entity(indices = [Index("url", unique = true)])
data class VideoRecord(
    val url: String,
    var progress: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}