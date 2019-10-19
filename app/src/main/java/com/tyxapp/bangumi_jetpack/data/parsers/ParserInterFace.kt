package com.tyxapp.bangumi_jetpack.data.parsers

import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.CategorItem
import com.tyxapp.bangumi_jetpack.data.Listing

/**
 * 主页解析接口
 *
 */
interface IHomePageParser {

    /**
     * 主页数据列表
     * @return 键为类别, 值为该类别番剧
     */
    fun getHomeBangumis(): Map<String, List<Bangumi>>

    /**
     * 获取分类列表
     *
     */
    fun getCategorItems(): List<CategorItem>

    /**
     * 根据类别词获取该类别番剧
     *
     */
    fun getCategoryBangumis(category: String): Listing<Bangumi>

    /**
     * 获取时间表
     *
     */
    fun getBangumiTimeTable(): List<List<Bangumi>>
}

/**
 * 搜索结果获取接口
 *
 */
interface IsearchParse {
    fun getSearchResult(searchWord: String): Listing<Bangumi>
}