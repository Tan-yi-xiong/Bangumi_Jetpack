package com.tyxapp.bangumi_jetpack.data.parsers

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLDecoder

private const val BASE_URL = "http://www.qimiqimi.co"

class Qimi : IsearchParser, IPlayerVideoParser {

    private var detailDocument: Document? = null

    private fun initDetailDocument(id: String) {
        synchronized(Qimi::class.java) {
            if (detailDocument == null) {
                val url = "$BASE_URL/detail/$id.html"
                detailDocument = Jsoup.parse(OkhttpUtil.getResponseData(url))
            }
        }
    }

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        initDetailDocument(id)
        val parentElement = detailDocument!!.getElementsByClass("detail-cols fn-clear")[0]
        val imgTag = parentElement.get_img_tags()[0]
        val name = imgTag.attrAlt()
        val cover = imgTag.attrSrc().run {
            if (contains("http")) {
                this
            } else {
                "$BASE_URL$this"
            }
        }
        val cast = parentElement.getElementsByClass("nyzhuy").getOrNull(0)?.run {
            buildString {
                this@run.get_a_tags().forEach { aTag ->
                    append(aTag.text())
                    append("\n")
                }
            }
        } ?: ""

        val ji = parentElement.getElementsByClass("color").text()

        val FRTag = parentElement.getElementsByClass("fn-right")
        val niandai = FRTag.getOrNull(2)?.get_a_tags()?.text() ?: ""
        val type = try {
            parentElement.getElementsByClass("fn-left").run {
                buildString {
                    this@run[6].get_a_tags().forEach { aTag ->
                        append(aTag.text())
                        append(" ")
                    }
                }
            }
        }catch (e: Exception) {
            ""
        }
        val intro = detailDocument!!.getElementsByClass("tjuqing").takeIf { it.isNotEmpty() }?.run {
            get(0).text()
        } ?: ""
        BangumiDetail(
            id = id,
            name = name,
            cover = cover,
            source = BangumiSource.Qimi,
            intro = intro,
            jiTotal = ji,
            niandai = niandai,
            type = type,
            cast = cast
        )
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            initDetailDocument(id)
            val jiList = ArrayList<JiItem>()
            val linesElement = detailDocument!!.getElementsByClass("video_list fn-clear")
            val line = linesElement.size
            if (line > 0) {
                linesElement.forEach { lineElement ->
                    if (jiList.isNotEmpty()) return@forEach
                    lineElement.children().forEach {
                        jiList.add(JiItem(it.text()))
                    }
                }
            }
            Pair(line, jiList)
        }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/play/$id/${line + 1}/${ji + 1}.html"
            try {
                val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
                val jsonData = document.getElementById("bofang_box").child(0).toString().run {
                    substring(indexOf("{"), lastIndexOf("}") + 1)
                }
                val jsonObject = JSONObject(jsonData)
                val playUrl = jsonObject.getString("url").run {
                    val base64DecodeString = Base64.decode(this, Base64.DEFAULT)
                    URLDecoder.decode(String(base64DecodeString), "utf-8")
                }
                VideoUrl(playUrl)
            } catch (e: Exception) {
                VideoUrl(url, true)
            }
        }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            initDetailDocument(id)
            val bangumis = ArrayList<Bangumi>()
            detailDocument!!.getElementsByClass("img-list dis").forEach { bangumisElement ->
                bangumisElement.children().forEach { bangumiElement ->
                    val imgtag = bangumiElement.get_img_tags()[0]
                    val name = imgtag.attrAlt()
                    val cover = "$BASE_URL${imgtag.attrSrc()}"
                    val bagnumiId = getHtmlBangumiId(bangumiElement.get_a_tags()[0].attrHref())!!
                    bangumis.add(Bangumi(bagnumiId, BangumiSource.Qimi, name, cover))
                }
            }
            bangumis
        }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? {
        return null
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factory = QimiSearchResultDataSourchFactory(searchWord)
        val pageList = LivePagedListBuilder(factory, 8).build()
        return Listing(
            liveDataPagelist = pageList,
            netWordState = factory.dataSourceLiveData.switchMap { it.netWordState },
            initialLoad = factory.dataSourceLiveData.switchMap { it.initialLoadLiveData },
            retry = { factory.dataSourceLiveData.value!!.retry() }
        )
    }

}

private class QimiSearchResultDataSourch(
    searchWord: String
) : PageResultDataSourch<Int, Bangumi>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>
    ) {

        val url = "$BASE_URL/vod/search.html?wd=$encodeSearchWord"
        val result = parserBangumiFromUrl(Jsoup.parse(OkhttpUtil.getResponseData(url)))
        callback.onResult(result, null, 2)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    private fun parserBangumiFromUrl(document: Document): List<Bangumi> {
        val bangumisElemet = document.getElementsByClass("show-list")[0]
        val bangumis = ArrayList<Bangumi>()
        if (bangumisElemet.children().isEmpty()) return emptyList()

        bangumisElemet.children().forEach { bangumiElement ->
            val parentElement = bangumiElement.getElementsByClass("play-txt")[0]
            val id = getHtmlBangumiId(parentElement.get_a_tags()[0].attrHref())!!
            val name = parentElement.get_a_tags()[0].text()
            val cover = bangumiElement.getElementsByClass("play-img")[0]
                .get_img_tags()[0]
                .attrSrc().run {
                    if (this.contains("http")) {
                        this
                    } else {
                        "$BASE_URL$this"
                    }
                }
            val ji = bangumiElement.getElementsByTag("span")[0].text()
            bangumis.add(Bangumi(id, BangumiSource.Qimi, name, cover, ji))
        }
        return bangumis
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {
        val page = params.key
        val url = "$BASE_URL/vod/search/wd/$encodeSearchWord/page/$page.html"
        val result = parserBangumiFromUrl(Jsoup.parse(OkhttpUtil.getResponseData(url)))
        callback.onResult(result, if (result.isEmpty()) null else page + 1)
    }

}

private class QimiSearchResultDataSourchFactory(
    private val searchWord: String
) : DataSource.Factory<Int, Bangumi>() {
    val dataSourceLiveData = MutableLiveData<QimiSearchResultDataSourch>()

    override fun create(): DataSource<Int, Bangumi> {
        return QimiSearchResultDataSourch(searchWord).apply {
            dataSourceLiveData.postValue(this)
        }
    }

}