package com.tyxapp.bangumi_jetpack.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bangumi(
        @ColumnInfo(name = "video_id")
        val id: String,

        val source: BangumiSource,
        var name: String = "",
        var cover: String = "",//封面
        var jiTotal: String = ""//集数
) {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var dbId: Int = 0
}