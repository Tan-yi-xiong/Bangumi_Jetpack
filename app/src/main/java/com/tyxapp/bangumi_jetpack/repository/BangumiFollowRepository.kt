package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.db.BangumiDetailDao

class BangumiFollowRepository(
    private val bangumiDetailDao: BangumiDetailDao
) {
    fun getFollowBangumis() = bangumiDetailDao.getFollowBangumis()

    suspend fun removeFromFollow(bangumiDetail: BangumiDetail) {
        bangumiDetail.isFollow = false
        bangumiDetailDao.update(bangumiDetail)
    }

    companion object{
        private var INSTANCE: BangumiFollowRepository? = null

        fun getInstance(): BangumiFollowRepository {
            INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = BangumiFollowRepository(AppDataBase.getInstance().bangumiDetailDao())
                }
            }
            return INSTANCE!!
        }
    }
}