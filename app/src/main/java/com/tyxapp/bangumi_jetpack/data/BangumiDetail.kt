package com.tyxapp.bangumi_jetpack.data

import androidx.room.*

/**
 * 番剧详细信息, 视频播放页面使用
 *
 */
@Entity(tableName = "bangumi_detail", indices = [Index("vod_id", "source", unique = true)])
data class BangumiDetail(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var dbId: Long = 0,

    @ColumnInfo(name = "vod_id") var id: String,

    var source: BangumiSource,

    var name: String,

    var jiTotal: String,//集数

    var isFollow: Boolean = false,//是否为追番

    var lastWatchTime: Long = 0L,//最后观看时间

    var lastWatchJi: Int = 0, // 上次看到的集数

    var lastWatchLine: Int = 0, // 上次观看的线路

    var isDownLoad: Boolean = false, //是否有下载的内容

    var cover: String = "",//封面

    @Ignore val niandai: String = "",//年代

    @Ignore val cast: String = "",//声优

    @Ignore val staff: String = "",//制作

    @Ignore val type: String = "",//番剧类型

    @Ignore val intro: String = ""//简介
) {

    constructor() : this(
        0L, "", BangumiSource.Zzzfun, "", "", false, 0L, 0, 0, false, "", "",
        "", "", "", ""
    )
}