package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.CategoryBangumi
import com.tyxapp.bangumi_jetpack.data.Listing
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.parsers.IHomePageParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class HomeDataRepository(
    private val iHomePageParser: IHomePageParser
) {
    private val bangumiDetailDao = AppDataBase.getInstance().bangumiDetailDao()

    suspend fun getHomeBangumis() = iHomePageParser.getHomeBangumis()

    suspend fun getCategorItems() = iHomePageParser.getCategorItems()

    suspend fun getBangumiTimeTable(): List<List<Bangumi>> = iHomePageParser.getBangumiTimeTable()

    fun getCategoryBangumis(category: String): Listing<CategoryBangumi> = iHomePageParser.getCategoryBangumis(category)

    /**
     * 类别页面点击追番/取消追番
     *
     */
    suspend fun changeBangumiFollowState(bangumi: Bangumi, isFollow: Boolean) {
        val bangumiDetail = bangumiDetailDao.getBangumiDetail(bangumi.id, bangumi.source.name)
        if (bangumiDetail == null) {
            bangumiDetailDao.insert(
                BangumiDetail(
                    id = bangumi.id,
                    cover = bangumi.cover,
                    source = bangumi.source,
                    name = bangumi.name,
                    isFollow = isFollow,
                    jiTotal = ""
                )
            )
        } else {
            bangumiDetail.isFollow = isFollow
            bangumiDetailDao.update(bangumiDetail)
        }
    }
}