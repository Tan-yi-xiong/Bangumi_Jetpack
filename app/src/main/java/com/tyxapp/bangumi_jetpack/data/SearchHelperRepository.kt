package com.tyxapp.bangumi_jetpack.data

import com.tyxapp.bangumi_jetpack.data.db.SearchWordDao
import com.tyxapp.bangumi_jetpack.data.parsers.IsearchParse
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil
import com.tyxapp.bangumi_jetpack.utilities.forEach
import com.tyxapp.bangumi_jetpack.utilities.info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

private const val SEARCH_HELPER_URL = "http://www.qimiqimi.co/index.php/ajax/suggest?mid=1&wd="
class SearchHelperRepository(
    private val searchWordDao: SearchWordDao
) {
    suspend fun getSearchWords(key: String): List<SearchWord> {
        return if (key.isEmpty()) {
            getFromDb()
        } else {
            getFromNet(key)
        }
    }

    private suspend fun getFromNet(key: String): List<SearchWord> = withContext(Dispatchers.IO) {
        try {
            val jsonData = OkhttpUtil.getResponseData("$SEARCH_HELPER_URL${URLEncoder.encode(key, "UTF-8")}")
            val jsonObject = JSONObject(jsonData).takeIf { !it.isNull("list") }
                ?: return@withContext emptyList<SearchWord>()

            return@withContext jsonObject.getJSONArray("list").run {
                val searchWords = ArrayList<SearchWord>()
                forEach {
                    val searchWord = SearchWord(
                        word = it.getString("name"),
                        isFromNet = true
                    )
                    searchWords.add(searchWord)
                }
                searchWords
            }
        } catch (e: Exception) {
            info("$e searchHelper")
            emptyList<SearchWord>()
        }
    }

    suspend fun insertSearchWord(searchWord: SearchWord) = withContext(Dispatchers.IO) {
        searchWordDao.insert(searchWord)
    }

    private suspend fun getFromDb(): List<SearchWord> = withContext(Dispatchers.IO) {
        searchWordDao.getSearchWords()
    }

    suspend fun removeSearchWord(searchWord: SearchWord) = withContext(Dispatchers.IO) {
        searchWordDao.deleteSearchWord(searchWord)
    }
}