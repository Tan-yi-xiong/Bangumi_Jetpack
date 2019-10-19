package com.tyxapp.bangumi_jetpack.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.data.SearchWord

@Database(entities = [SearchWord::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun searchDao(): SearchWordDao

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
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}