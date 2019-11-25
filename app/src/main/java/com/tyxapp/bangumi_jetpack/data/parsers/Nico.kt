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
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private const val BASE_URL = "http://www.nicotv.club"

class Nico : IsearchParser, IPlayerVideoParser {

    private lateinit var detaliHtml: String

    private val linesWithPlayerHtmlUrl by lazy(LazyThreadSafetyMode.NONE) { SparseArray<List<String>>() }

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/video/detail/$id.html"
        detaliHtml = OkhttpUtil.getResponseData(url)
        val document = Jsoup.parse(detaliHtml)

        val bangumiDetailElement = document.getElementsByClass("media-body")[0]
        val name = bangumiDetailElement.get_a_tags().takeIf { it.size > 0 }?.run {
            get(0).text()
        } ?: ""

        val ji =
            bangumiDetailElement.getElementsByTag("small").text().split(" ").run { get(size - 1) }
        val ftrElements = bangumiDetailElement.getElementsByClass("ff-text-right")
        val cast=
            ftrElements.takeIf { it.size > 0 }
                ?.run { parserCast(this[0]) }
                ?.replace(" ", "\n")
                ?: ""
        val daoyan = ftrElements.takeIf { it.size > 2 }?.run {
            get(1).get_a_tags().takeIf { it.size > 0 }?.get(0)?.text() ?: ""
        } ?: ""

        val type = ftrElements.takeIf { it.size > 3 }
            ?.run { parserCast(this[2]) } ?: ""

        val niandai = ftrElements.takeIf { it.size > 5 }?.run {
            get(4).get_a_tags()[0].text()
        } ?: ""

        val cover = document.getElementsByClass("media-object img-thumbnail ff-img").attr("data-original")

        val neirong =
            bangumiDetailElement.getElementsByClass("vod-content ff-collapse text-justify").text()

        BangumiDetail(
            id = id,
            source = BangumiSource.Nico,
            cover = cover,
            name = name,
            jiTotal = ji,
            cast = cast,
            staff = daoyan,
            type = type,
            niandai = niandai,
            intro = neirong
        )
    }

    private fun parserCast(element: Element): String? {
        return buildString {
            element.get_a_tags().forEach {
                append(it.text())
                append(" ")
            }
            if (length > 0) deleteCharAt(length - 1)
        }
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/video/detail/$id.html"
            val document = if (::detaliHtml.isInitialized) {
                Jsoup.parse(detaliHtml)
            } else {
                Jsoup.parse(OkhttpUtil.getResponseData(url))
            }

            val lineElements = document.getElementsByClass("tab-content ff-playurl-tab").takeIf {
                it.size > 0
            }?.get(0) ?: return@withContext Pair(-1, emptyList<JiItem>())

            val jilist = ArrayList<JiItem>()
            var line = 0
            lineElements.children().forEachIndexed { _, jiListElement ->
                if (jiListElement.children().hasClass("col-md-3 col-xs-12")) return@forEachIndexed

                val playerHtmlUrls = ArrayList<String>()
                jiListElement.children().forEach {
                    val aTag = it.get_a_tags()[0]
                    if (jilist.size < jiListElement.children().size) {
                        jilist.add(JiItem(aTag.text()))
                    }
                    playerHtmlUrls.add(BASE_URL + aTag.attrHref())
                }
                linesWithPlayerHtmlUrl.append(line, playerHtmlUrls)
                line++
            }
            Pair(line, jilist)
        }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl =
        withContext(Dispatchers.IO) {
            if (line >= linesWithPlayerHtmlUrl.size()) {
                VideoUrl("$BASE_URL/video/detail/$id.html", true)
            } else {
                val url = linesWithPlayerHtmlUrl[line].takeIf { ji < it.size }
                    ?.get(ji)
                    ?: return@withContext VideoUrl("$BASE_URL/video/detail/$id.html", true)

                val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
                val playerUrl = BASE_URL + document.getElementById("cms_player")
                    .getElementsByTag("script")[0]
                    .attrSrc()
                val playerUrlJson = OkhttpUtil.getResponseData(playerUrl).run {
                    substring(indexOf("{"), lastIndexOf("}") + 1)
                }


                parserToPlayerUrl(playerUrlJson) ?: VideoUrl(
                    "$BASE_URL/video/detail/$id.html",
                    true
                )
            }
        }

    private fun parserToPlayerUrl(playerUrlJson: String): VideoUrl? {
        val jsonObject = JSONObject(playerUrlJson)
        val name = jsonObject.getString("name")
        val url = jsonObject.getString("url")
        val time = jsonObject.getString("time")
        val auth_key = jsonObject.getString("auth_key")
        val playerUrl: String

        if (url.contains(".mp4")) {
            playerUrl = url.substring(url.indexOf("=") + 1)
        } else if ("kkm3u8" == name) {
            playerUrl = url
        } else if ("360biaofan" == name ||
            url.contains("tyjx2.kingsnug.cn") && name == "haokan_baidu") {

            val htmlUrl = "$url&time=$time&auth_key=$auth_key"
            var data = OkhttpUtil.getResponseData(htmlUrl)
            val document = Jsoup.parse(data)
            val element = document.getElementsByTag("script")[1]
            data = element.toString()
            data = data.substring(data.lastIndexOf("{"), data.lastIndexOf(","))
            playerUrl = data.substring(data.indexOf("\"") + 1, data.lastIndexOf("\""))

        } else {
            return null
        }
        return VideoUrl(playerUrl)
    }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/video/detail/$id.html"
            val document = if (::detaliHtml.isInitialized) {
                Jsoup.parse(detaliHtml)
            } else {
                Jsoup.parse(OkhttpUtil.getResponseData(url))
            }
            parserBangumiFromHtml(document)
        }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? {
        return null
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factory = NicoSearchResultDataSourceFactory(searchWord)
        val livePagedList = LivePagedListBuilder(factory, 10).build()

        return Listing(
            liveDataPagelist = livePagedList,
            netWordState = factory.NicoSearchResultDataSourceLiveData.switchMap { it.netWordState },
            retry = { factory.NicoSearchResultDataSourceLiveData.value?.retry() },
            initialLoad = factory.NicoSearchResultDataSourceLiveData.switchMap { it.initialLoadLiveData }
        )
    }

}

private fun parserBangumiFromHtml(document: Document): List<Bangumi> {
    val bangumiElements = document.getElementsByClass("col-md-2 col-sm-3 col-xs-4")
    val bangumis = ArrayList<Bangumi>()

    bangumiElements.forEach { elemetn ->
        val bangumiElement =
            elemetn.takeIf { it.get_a_tags().size > 0 }?.get_a_tags()?.get(0) ?: return@forEach

        val id = getHtmlBangumiId(bangumiElement.attrHref()) ?: ""

        val imgTag = bangumiElement.get_img_tags()[0]
        val name = imgTag.attrAlt()
        val cover = imgTag.attr("data-original")
        val ji = bangumiElement.getElementsByTag("span").text()

        bangumis.add(Bangumi(id, BangumiSource.Nico, name, cover, ji))
    }
    return bangumis
}


private class NicoSearchResultDataSource(
    searchWord: String
) : PageResultDataSourch<Int, Bangumi>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>) {

        val url = "$BASE_URL/video/search/$encodeSearchWord.html"

        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val haveNext = document.getElementsByClass("disabled").isNotEmpty()
        val result = parserBangumiFromHtml(document)

        callback.onResult(result, null, if (haveNext) 2 else null)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {
        val page = params.key

        val url = "$BASE_URL/vod-search-wd-$encodeSearchWord-p-$page.html"
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val currentPage =
            document.getElementsByClass("disabled")[0].get_a_tags()[0].text().toInt()
        if (page > currentPage) { // 没有下一页
            callback.onResult(emptyList(), null)
        } else {
            callback.onResult(parserBangumiFromHtml(document), page + 1)
        }
    }

}

private class NicoSearchResultDataSourceFactory(
    private val searchWord: String
) : DataSource.Factory<Int, Bangumi>() {

    val NicoSearchResultDataSourceLiveData = MutableLiveData<NicoSearchResultDataSource>()

    override fun create(): DataSource<Int, Bangumi> {
        return NicoSearchResultDataSource(searchWord).apply {
            NicoSearchResultDataSourceLiveData.postValue(this)
        }
    }

}