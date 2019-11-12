package com.tyxapp.bangumi_jetpack.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo

@Dao
interface DownLoadInfoDao {
    @Insert
    fun insert(downLoadInfo: DownLoadInfo): Long

    @Query("SELECT * FROM DOWNLOADINFO WHERE state='DOWNLOADING' LIMIT :limit")
    suspend fun getDownLoading(limit: Int): List<DownLoadInfo>

    @Query("SELECT * FROM DOWNLOADINFO WHERE state='WAIT' LIMIT :limit")
    fun getWaitDownLoad(limit: Int): List<DownLoadInfo>

    @Query("SELECT id FROM DOWNLOADINFO WHERE videoUrl=:videoUrl")
    fun getDownLoadInfoBy(videoUrl: String): Long?

    @Query("SELECT * FROM DOWNLOADINFO WHERE id=:id")
    fun getDownLoadInfoBy(id: Int): DownLoadInfo?

    @Update
    fun update(downLoadInfo: DownLoadInfo)

    @Query("SELECT * FROM downloadinfo")
    suspend fun getAll(): List<DownLoadInfo>

    @Query("SELECT * FROM downloadinfo WHERE bangumiId=:bangumid AND bangumiSource=:bangumiSource")
    fun getBangumiDownLoadVideo(bangumid: String, bangumiSource: String): LiveData<List<DownLoadInfo>>

    @Query("SELECT * FROM downloadinfo WHERE bangumiId=:bangumid AND bangumiSource=:bangumiSource")
    suspend fun getBangumiDownLoadVideolist(bangumid: String, bangumiSource: String): List<DownLoadInfo>

    @Delete
    suspend fun delete(downLoadInfo: DownLoadInfo)
}