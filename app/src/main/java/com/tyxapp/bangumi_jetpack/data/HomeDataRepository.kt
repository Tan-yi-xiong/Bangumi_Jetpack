package com.tyxapp.bangumi_jetpack.data

import com.tyxapp.bangumi_jetpack.data.parsers.IHomePageParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeDataRepository(private val iHomePageParser: IHomePageParser) {
    suspend fun getHomeBangumis() = withContext(Dispatchers.IO) { iHomePageParser.getHomeBangumis() }

    suspend fun getCategorItems() = withContext(Dispatchers.IO) { iHomePageParser.getCategorItems() }

    suspend fun getBangumiTimeTable() = withContext(Dispatchers.IO) { iHomePageParser.getBangumiTimeTable() }

    fun getCategoryBangumis(category: String) = iHomePageParser.getCategoryBangumis(category)

    suspend fun refreshHomeBangumis() = getHomeBangumis()

}