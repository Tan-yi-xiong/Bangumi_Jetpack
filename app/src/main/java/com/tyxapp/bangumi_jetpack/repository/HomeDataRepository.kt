package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.parsers.IHomePageParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class HomeDataRepository(
    private val iHomePageParser: IHomePageParser
) {
    suspend fun getHomeBangumis() = iHomePageParser.getHomeBangumis()

    suspend fun getCategorItems() = iHomePageParser.getCategorItems()

    suspend fun getBangumiTimeTable() = iHomePageParser.getBangumiTimeTable()

    fun getCategoryBangumis(category: String) = iHomePageParser.getCategoryBangumis(category)


}