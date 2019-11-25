package com.tyxapp.bangumi_jetpack.data.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tyxapp.bangumi_jetpack.data.BangumiDetail

@Dao
interface BangumiDetailDao {
    @Insert
    suspend fun insert(bangumiDetail: BangumiDetail): Long

    @Query("SELECT * FROM bangumi_detail WHERE vod_id=:id AND source=:bangumiSource")
    suspend fun getBangumiDetail(id: String, bangumiSource: String): BangumiDetail?

    @Query("SELECT * FROM bangumi_detail WHERE isFollow=1 ORDER BY id DESC")
    fun getFollowBangumis(): DataSource.Factory<Int, BangumiDetail>

    @Update
    suspend fun update(bangumiDetail: BangumiDetail)

    @Query("SELECT * FROM bangumi_detail ORDER BY lastWatchTime DESC")
    fun getHistoryBangumis(): DataSource.Factory<Int, BangumiDetail>

    @Query("SELECT * FROM BANGUMI_DETAIL WHERE isDownLoad=1")
    fun getDownloadBagnumi(): LiveData<List<BangumiDetail>>

    @Query("SELECT * FROM BANGUMI_DETAIL WHERE isDownLoad=1")
    fun getDownloadBagnumiList(): List<BangumiDetail>

    @Query("SELECT isFollow FROM BANGUMI_DETAIL WHERE vod_id=:id AND source=:bangumiSource")
    fun isFollowingBangumi(id: String, bangumiSource: String): Boolean
}