package com.tyxapp.bangumi_jetpack.data.parsers

import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import okhttp3.Request
import org.jetbrains.anko.collections.forEachWithIndex
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.RuntimeException
import java.net.URI
import java.net.URLDecoder

private const val BASE_URL = "https://www.malimali.tv"


class Malimali : IsearchParser, IPlayerVideoParser {
    private var playerDetailDocument: Document? = null
    private val lineWithPlayerurls by lazy(LazyThreadSafetyMode.NONE) { SparseArray<List<String>>() }

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/voddetail/$id.html"
        playerDetailDocument =
            Jsoup.parse(OkhttpUtil.getResponseData(OkhttpUtil.createPhoneRequest(url)))

        val baseInfoElement =
            playerDetailDocument!!.getElementsByClass("leo-detail-media leo-po-re")[0]!!
        val (name, ji) = baseInfoElement.children()
            .getOrNull(0)
            ?.text()
            ?.split("|")
            ?.run { getOrNull(0)?.trim() to getOrNull(1)?.trim() }
            ?: Pair("", "")
        val makeInformationElements =
            baseInfoElement.getElementsByClass("leo-mt-20 leo-color-a").getOrNull(0)?.children()
        val staff = makeInformationElements?.getOrNull(0)?.run { parserStaffInfo(this) } ?: ""
        val cast = makeInformationElements?.getOrNull(1)?.run { parserStaffInfo(this) } ?: ""
        val (niandai, type) = makeInformationElements?.getOrNull(2)
            ?.children()
            ?.run { getOrNull(0)?.text() to getOrNull(1)?.text() }
            ?: Pair("", "")
        val intro = playerDetailDocument!!.getElementsByClass("leo-color-e leo-fs-s leo-ellipsis-2")
            ?.getOrNull(0)
            ?.text()
        val cover = playerDetailDocument!!.getElementsByClass("leo-lazy leo-radius-s")
            .getOrNull(0)
            ?.attrSrc()

        BangumiDetail(
            id = id,
            source = BangumiSource.Malimali,
            name = name ?: "",
            cover = cover ?: "",
            jiTotal = ji ?: "",
            niandai = niandai ?: "",
            staff = staff,
            cast = cast,
            type = type ?: "",
            intro = intro ?: ""
        )
    }

    private fun parserStaffInfo(element: Element): String {
        return element.getElementsByTag("a")
            .joinToString(separator = "\n") { it.text() }
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            if (playerDetailDocument == null) {
                val url = "$BASE_URL/voddetail/$id.html"
                playerDetailDocument =
                    Jsoup.parse(OkhttpUtil.getResponseData(OkhttpUtil.createPhoneRequest(url)))
            }
            var line = 0
            val jiList = ArrayList<JiItem>()

            playerDetailDocument!!.getElementsByClass("leo-play-num").forEach { lines ->
                line++
                val playurls = ArrayList<String>()
                lines.children().forEach { jiElement ->
                    val (name, plaUrl) = jiElement.get_a_tags()[0].run {
                        text() to attrHref()
                    }
                    if (jiList.size < lines.children().size) {
                        jiList.add(JiItem(name))
                    }
                    playurls.add(plaUrl)
                }
                lineWithPlayerurls.append(line, playurls)
            }
            Pair(line, jiList)
        }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl {
        return if (line >= lineWithPlayerurls.size()) {
            VideoUrl("$BASE_URL/voddetail/$id.html", true)
        } else {
            withContext(Dispatchers.IO) {
                val url = "$BASE_URL${lineWithPlayerurls[line + 1][ji]}"
                val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
                val playJSONObject = document.getElementById("player").child(0).toString().run {
                    JSONObject(substring(indexOf("{"), lastIndexOf("}") + 1))
                }
                VideoUrl(playJSONObject.getString("url"), false)
            }
        }
    }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            if (playerDetailDocument == null) {
                val url = "$BASE_URL/voddetail/$id.html"
                playerDetailDocument =
                    Jsoup.parse(OkhttpUtil.getResponseData(OkhttpUtil.createPhoneRequest(url)))
            }
            val elements =
                playerDetailDocument!!.getElementsByClass("swiper-slide leo-video-ritem leo-col-4 leo-left leo-mb-30 leo-mr-20")
            val bangumis = ArrayList<Bangumi>()
            elements.forEach { bangumisElements ->
                bangumisElements.children().forEach { bangumiElement ->
                    val bangumiId = parserid(bangumiElement.attrHref())!!
                    val name = bangumiElement.attr("title")
                    val cover = bangumiElement.getElementsByTag("img")
                        ?.getOrNull(0)
                        ?.attrSrc()
                        ?: ""
                    val ji =
                        bangumiElement.getElementsByClass("leo-video-remark leo-po-re leo-fs-s leo-color-a leo-right leo-ellipsis-1")
                            ?.getOrNull(0)
                            ?.text() ?: ""
                    bangumis.add(Bangumi(bangumiId, BangumiSource.Malimali, name, cover, ji))
                }
            }
            bangumis
        }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? {
        return null
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factory = MalimaliSearchResultDataSourceFactory(searchWord)

        val pageList = LivePagedListBuilder(factory, 100).build()

        return Listing(
            liveDataPagelist = pageList,
            netWordState = factory.malimaliSearchResultDataSourceLiveData.switchMap { it.netWordState },
            initialLoad = factory.malimaliSearchResultDataSourceLiveData.switchMap { it.initialLoadLiveData },
            retry = { factory.malimaliSearchResultDataSourceLiveData.value!!.retry() }
        )
    }

}

private fun parserid(idStr: String) = Regex("""\d+""").find(idStr)?.value

class MalimaliSearchResultDataSource(
    searchWord: String
) : PageResultDataSourch<Int, Bangumi>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>
    ) {

        val url = "$BASE_URL/vodsearch/$encodeSearchWord----------1---.html"

        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val result = parserBangumiFromHtml(document)
        callback.onResult(result, null, 2)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {
        val page = params.key
        val url = "$BASE_URL/vodsearch/$encodeSearchWord----------$page---.html"
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val result = parserBangumiFromHtml(document)

        if (result.isEmpty()) {
            callback.onResult(result, null)
        } else {
            callback.onResult(result, page + 1)
        }
    }

    private fun parserBangumiFromHtml(document: Document): List<Bangumi> {
        val bangumisElements = document.getElementsByClass("search list")
        bangumisElements.takeIf { it.isNotEmpty() } ?: return emptyList()
        val bangumis = ArrayList<Bangumi>()
        bangumisElements.forEach { element ->
            val (cover, name) = element.getElementsByClass("item-lazy")
                .getOrNull(0)
                ?.run { attr("data-echo") to attr("alt") }
                ?: Pair("", "")

            val ji = element.getElementsByClass("so-imgTag_rb")
                .getOrNull(0)
                ?.text()
                ?: ""

            val id = element.getElementsByTag("a")
                .getOrNull(0)
                ?.run { parserid(attrHref()) }
                ?: return@forEach

            bangumis.add(Bangumi(id, BangumiSource.Malimali, name, cover, ji))
        }
        return bangumis
    }

}

class MalimaliSearchResultDataSourceFactory(
    private val searchWord: String
) : DataSource.Factory<Int, Bangumi>() {
    val malimaliSearchResultDataSourceLiveData = MutableLiveData<MalimaliSearchResultDataSource>()
    override fun create(): DataSource<Int, Bangumi> {

        return MalimaliSearchResultDataSource(searchWord).apply {
            malimaliSearchResultDataSourceLiveData.postValue(this)
        }
    }

}
