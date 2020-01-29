package com.tyxapp.bangumi_jetpack.data.parsers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import kotlin.collections.ArrayList


private const val BASE_URL = "http://m.imomoe.in"


class Sakura : IsearchParser, IPlayerVideoParser {
    private val videoUrls by lazy(LazyThreadSafetyMode.NONE) { ArrayList<String>() }

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/view/$id.html"
        val html = String(OkhttpUtil.getResponseBody(url).bytes(), charset("gb2312"))
        val document = Jsoup.parse(html)
        val parentElement = document.getElementsByClass("am-g am-intro-bd")[0]
        val imgTag = parentElement.getElementsByClass("am-intro-left am-u-sm-5")[0].get_img_tags()[0]
        val name = imgTag.attrAlt()
        val cover = imgTag.attrSrc()
        val type = parentElement.getElementsByClass("am-icon-tags")[0].run {
            buildString {
                get_a_tags().forEach {
                    append(it.text())
                    append(" ")
                }
            }
        }
        val ji = parentElement.getElementsByClass("red").text()
        val niandai = parentElement.getElementsByClass("am-icon-tag")[0].get_a_tags().text()
        val intor = document.getElementsByClass("txtDesc autoHeight").text()
        BangumiDetail(
            id = id,
            source = BangumiSource.Sakura,
            name = name,
            cover = cover,
            jiTotal = ji,
            type = type,
            intro = intor,
            niandai = niandai
        )
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/player/$id-0-0.html"
            val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
            val jiUrl = document.getElementsByClass("player")[0].child(0).attrSrc().run {
                "$BASE_URL$this"
            }

            Pair(1, parserJi(OkhttpUtil.getResponseData(jiUrl)))
        }

    private fun parserJi(text: String): List<JiItem> {
        val jiList = ArrayList<JiItem>()
        val lines = text.substring(text.indexOf('['), text.lastIndexOf(']') - 1).run { split("],") }
        lines.forEach {
            if (jiList.isNotEmpty()) return jiList
            if (!it.contains("hd_iask") && !it.contains("http")) return@forEach
            val jiData = it.substring(it.lastIndexOf('[') + 1, it.lastIndexOf(']')).run { split("','") }
            jiData.forEach { jiText ->
                var cpJiText = jiText
                if (jiText.contains("'")) {
                    cpJiText = jiText.replace("'", "")
                }
                val jiSplit = cpJiText.split("$")

                if ("hd_iask" == jiSplit[2] || (jiSplit[1].contains("http") && !jiSplit[1].contains(".html"))) {
                    jiList.add(JiItem(unicodeToString(jiSplit[0])))
                    videoUrls.add(jiSplit[1])
                }
            }
        }
        return jiList
    }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl {
        return try {
            VideoUrl(videoUrls[ji])
        } catch (e: Exception) {
            VideoUrl("$BASE_URL/view/$id.html", true)
        }
    }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/player/$id-0-0.html"
        val html = String(OkhttpUtil.getResponseBody(url).bytes(), charset("gb2312"))
        val document = Jsoup.parse(html)
        parserBangumiFromHtml(document)
    }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? {
        return null
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factory = SakuraSearchResultDataSourceFactory(searchWord)

        val pageList = LivePagedListBuilder(factory, 8).build()

        return Listing(
            liveDataPagelist = pageList,
            netWordState = factory.sakuraSearchResultDataSourceLiveData.switchMap { it.netWordState },
            initialLoad = factory.sakuraSearchResultDataSourceLiveData.switchMap { it.initialLoadLiveData },
            retry = { factory.sakuraSearchResultDataSourceLiveData.value!!.retry() }
        )
    }

}

private fun parserBangumiFromHtml(document: Document): List<Bangumi> {
    val bangumis = ArrayList<Bangumi>()
    val bangumisElement = document.getElementsByClass("am-gallery-item")
    bangumisElement.forEach { bangumiElement ->
        val id = getHtmlBangumiId(bangumiElement.get_a_tags()[0].attrHref())!!
        val name = bangumiElement.getElementsByTag("h3").takeIf { it.isNotEmpty() }?.text() ?: ""
        val cover = bangumiElement.getElementsByClass("lazy").takeIf { it.isNotEmpty()  }
            ?.attr("data-original")
            ?: ""
        val ji = bangumiElement.getElementsByClass("am-gallery-desc").takeIf { it.isNotEmpty() }?.run {
            text().split(" ").run { get(size - 1) }
        } ?: ""
        bangumis.add(Bangumi(id, BangumiSource.Sakura, name, cover, ji))
    }
    return bangumis
}

class SakuraSearchResultDataSource(
    private val searchWord: String
) : PageResultDataSourch<String, Bangumi>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Bangumi>) {

        val url = "$BASE_URL/search.asp?searchword=${URLEncoder.encode(searchWord, "gb2312")}"
        val result: Pair<String?, List<Bangumi>> = getCallbackResult(url)

        callback.onResult(result.second, null, result.first)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.second.isEmpty()))
    }

    override fun afterload(params: LoadParams<String>, callback: LoadCallback<String, Bangumi>) {
        val result: Pair<String?, List<Bangumi>> = getCallbackResult(params.key)
        callback.onResult(result.second, result.first)
    }

    private fun getCallbackResult(url: String): Pair<String?, List<Bangumi>> {
        val html = String(OkhttpUtil.getResponseBody(url).bytes(), charset("gb2312"))
        val document: Document = Jsoup.parse(html)
        val result: List<Bangumi> = parserBangumiFromHtml(document)

        val netxTag = document.getElementsByClass("am-pagination-next")[0].get_a_tags()[0]
        val nextKey = if (netxTag.hasAttr("href")) {
            "$BASE_URL/search.asp${netxTag.attrHref()}"
        } else {
            null
        }
        return Pair(nextKey, result)
    }
}

private class SakuraSearchResultDataSourceFactory(
    private val searchWord: String
) : DataSource.Factory<String, Bangumi>() {

    val sakuraSearchResultDataSourceLiveData = MutableLiveData<SakuraSearchResultDataSource>()

    override fun create(): DataSource<String, Bangumi> {
        return SakuraSearchResultDataSource(searchWord).apply {
            sakuraSearchResultDataSourceLiveData.postValue(this)
        }
    }

}