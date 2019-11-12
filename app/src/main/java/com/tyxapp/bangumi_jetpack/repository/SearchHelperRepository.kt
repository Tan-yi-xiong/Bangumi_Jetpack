package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.SearchWord
import com.tyxapp.bangumi_jetpack.data.db.SearchWordDao
import com.tyxapp.bangumi_jetpack.data.parsers.SearchWordParser
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil
import com.tyxapp.bangumi_jetpack.utilities.forEach
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URLEncoder


class SearchHelperRepository(
    private val searchWordDao: SearchWordDao,
    private val searchWordParser: SearchWordParser
) {

    suspend fun getSearchWords(key: String): List<SearchWord> {
        return if (key.isEmpty()) {//没有输入内容默认为获取历史搜索
            searchWordDao.getSearchWords()//数据库获取
        } else {
            delay(20)//防止输入过快, 频繁请求
            searchWordParser.getSearchWord(key)//网络获取
        }
    }

    suspend fun insertSearchWord(searchWord: SearchWord) = searchWordDao.insert(searchWord)

    suspend fun removeSearchWord(searchWord: SearchWord) =
        searchWordDao.deleteSearchWord(searchWord)

}