package com.tyxapp.bangumi_jetpack.data.parsers

import com.tyxapp.bangumi_jetpack.data.SearchWord
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil
import com.tyxapp.bangumi_jetpack.utilities.forEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URLEncoder

private const val SEARCH_HELPER_URL = "http://www.qimiqimi.co/index.php/ajax/suggest?mid=1&wd="

class SearchWordParser {
    suspend fun getSearchWord(keyWord: String): List<SearchWord> = withContext(Dispatchers.IO) {
        try {
            val encodeWord = URLEncoder.encode(keyWord, "UTF-8")
            val jsonData = OkhttpUtil.getResponseData("$SEARCH_HELPER_URL$encodeWord")

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
            emptyList<SearchWord>()
        }
    }

    companion object{
        private var INSTANCE: SearchWordParser? = null

        fun getInstance(): SearchWordParser {
            INSTANCE ?: synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = SearchWordParser()
                }
            }
            return INSTANCE!!
        }
    }
}