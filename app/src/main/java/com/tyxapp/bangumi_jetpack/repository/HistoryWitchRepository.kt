package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.db.BangumiDetailDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryWitchRepository(
    private val bangumiDetailDao: BangumiDetailDao
) {
    fun getHistoryWitchBagnumis() = bangumiDetailDao.getHistoryBangumis()

    suspend fun remove(bangumiDetail: BangumiDetail) = withContext(Dispatchers.IO) {
        bangumiDetail.lastWatchTime = 0
        bangumiDetailDao.update(bangumiDetail)
    }

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