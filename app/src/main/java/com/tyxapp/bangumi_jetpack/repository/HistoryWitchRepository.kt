package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.db.BangumiDetailDao

class HistoryWitchRepository(
    private val bangumiDetailDao: BangumiDetailDao
) {
    fun getHistoryWitchBagnumis() = bangumiDetailDao.getHistoryBangumis()


    companion object{
        private var INSTANCE: HistoryWitchRepository? = null

        fun getInstance(): HistoryWitchRepository {
            INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = HistoryWitchRepository(AppDataBase.getInstance().bangumiDetailDao())
                }
            }
            return INSTANCE!!
        }
    }

}