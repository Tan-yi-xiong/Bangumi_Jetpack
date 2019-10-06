package com.tyxapp.bangumi_jetpack.data

import androidx.paging.DataSource

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
    fun getCategoryBangumis(category: String): DataSource.Factory<Int, Bangumi>

    /**
     * 获取时间表
     *
     */
    fun getBangumiTimeTable(): List<List<Bangumi>>
}