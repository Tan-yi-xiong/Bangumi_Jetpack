package com.tyxapp.bangumi_jetpack.data.db

import androidx.room.*
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.SearchWord
import com.tyxapp.bangumi_jetpack.data.VideoRecord

@Database(
    entities = [SearchWord::class, BangumiDetail::class, VideoRecord::class, DownLoadInfo::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun searchDao(): SearchWordDao
    abstract fun bangumiDetailDao(): BangumiDetailDao
    abstract fun videoRecordDao(): VideoRecordDao
    abstract fun downLoadInfoDao(): DownLoadInfoDao

    companion object {
        private var INSTANCE: AppDataBase? = null
        private const val DB_NAME = "Bangumi_DB"

        fun getInstance(): AppDataBase {
            INSTANCE ?: synchronized(AppDataBase::class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        BangumiApp.getContext(),
                        AppDataBase::class.java,
                        DB_NAME
                    ).allowMainThreadQueries()
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}