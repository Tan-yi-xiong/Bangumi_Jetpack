package com.tyxapp.bangumi_jetpack.data.parsers

import com.tyxapp.bangumi_jetpack.data.*
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

/**
 * 主页解析接口
 *
 */
interface IHomePageParser {

    /**
     * 主页数据列表
     * @return 键为类别, 值为该类别番剧
     */
    suspend fun getHomeBangumis(): Map<String, List<Bangumi>>

    /**
     * 获取分类列表
     *
     */
    suspend fun getCategorItems(): List<CategorItem>

    /**
     * 实际返回是Listing<CategoryBangumi>,不然会报错;  参考[Zzzfun], 早期接口设计失误。
     *
     */
    fun getCategoryBangumis(category: String): Listing<CategoryBangumi>

    /**
     * 获取时间表
     *
     */
    suspend fun getBangumiTimeTable(): List<List<Bangumi>>
}

/**
 * 搜索结果获取接口
 *
 */
interface IsearchParser {
    fun getSearchResult(searchWord: String): Listing<Bangumi>
}

/**
 * 播放页面数据接口, 播放页面使用
 *
 */
interface IPlayerVideoParser {
    /**
     * 获取番剧详细信息
     *
     */
    suspend fun getBangumiDetail(id: String): BangumiDetail

    /**
     *获取视频集数
     *
     *@return 左边为线路数量, 右边为集数集合
     */
    suspend fun getJiList(id: String): Pair<Int, List<JiItem>>

    /**
     * 获取视频播放地址, 如果获取失败[VideoUrl]的isJumpToBrowser设置为true, 会引导用户到浏览器看视频源
     *
     * @param id 番剧id
     * @param ji 集
     * @param line 线路
     */
    suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl

    /**
     * 获取推荐番剧
     *
     */
    suspend fun getRecommendBangumis(id: String): List<Bangumi>

    /**
     * 弹幕获取
     *
     * @return null为没有弹幕
     */
    suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser?
}