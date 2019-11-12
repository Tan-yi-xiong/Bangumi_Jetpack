package com.tyxapp.bangumi_jetpack.data.db

import androidx.room.*
import com.tyxapp.bangumi_jetpack.data.VideoRecord

@Dao
interface VideoRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(videoRecord: VideoRecord): Long

    @Query("SELECT * FROM VIDEORECORD WHERE url=:url")
    suspend fun getVideoRecord(url: String): VideoRecord?

    @Update
    suspend fun update(videoRecord: VideoRecord): Int

    @Delete
    suspend fun delete(videoRecord: VideoRecord): Int
}