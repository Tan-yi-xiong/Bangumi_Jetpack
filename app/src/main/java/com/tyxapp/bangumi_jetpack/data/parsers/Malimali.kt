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
import org.jetbrains.anko.collections.forEachWithIndex
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.net.URLDecoder

private const val BASE_URL = "http://www.malimali.com"


class Malimali : IsearchParser, IPlayerVideoParser {
    private var playerDetailDocument: Document? = null
    private val lineWithPlayerurls by lazy(LazyThreadSafetyMode.NONE) { SparseArray<List<String>>() }

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        val url = "$BASE_URL?m=vod-detail-id-$id.html"
        playerDetailDocument = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val pTags = playerDetailDocument!!.getElementsByClass("oi8EDlVc")[0].get_p_tags()
        val name = playerDetailDocument!!.getElementsByClass("oi8EDlVc")[0]
            .getElementsByTag("h1")
            .takeIf { it.size > 0 }
            ?.get(0)
            ?.text()
            ?: ""

        val ji = pTags.takeIf { it.size > 0 }?.let { elemetn ->
            elemetn[0].text().split(" ").run { get(size - 1) }
        } ?: ""

        val coverurl = playerDetailDocument!!.getElementsByClass("_1JapLbCo")[0]
            .get_img_tags()[0].attrSrc()
        val cover = if (coverurl.contains("http")) coverurl else "$BASE_URL$coverurl"

        val niandai = pTags.takeIf { it.size > 4 }?.get(3)?.text() ?: ""
        val intro = playerDetailDocument!!.getElementsByClass("mt10 _3mSSX7tA")
            .takeIf { it.size > 0 }?.get(0)?.text() ?: ""

        BangumiDetail(
            id = id,
            source = BangumiSource.Malimali,
            name = name,
            cover = cover,
            jiTotal = ji,
            niandai = niandai,
            intro = intro
        )

    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            if (playerDetailDocument == null) {
                val url = "$BASE_URL?m=vod-detail-id-$id.html"
                playerDetailDocument = Jsoup.parse(OkhttpUtil.getResponseData(url))
            }
            var line = 0
            val jiList = ArrayList<JiItem>()

            var videoUrl: String? = null
            playerDetailDocument!!.getElementsByClass("_1QUOPvL_ listurl").forEach { lines ->
                line++
                lines.children().forEach { jiElement ->
                    if (videoUrl == null) {
                        videoUrl = "$BASE_URL${jiElement.get_a_tags()[0].attrHref()}"
                    }
                    if (jiList.size < lines.children().size) {
                        val name = jiElement.get_p_tags()[0].text()

                        jiList.add(JiItem(name))
                    }
                }
            }

            // 获取所有线路视频播放地址
            videoUrl?.let { url ->
                val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
                val playurls =
                    document.getElementsByClass("fl playerbox iframe")[0].getElementsByTag("script")
                        .toString().run {
                            val s = substring(indexOf("(") + 1, lastIndexOf(")") - 1)
                            s.split("%24%24%24%")
                        }
                playurls.forEachWithIndex { index, urls ->
                    val spliturls = urls.split("%23")
                    val urlList = ArrayList<String>()
                    spliturls.forEach {
                        try {
                            val playurl = URLDecoder.decode(it.split("%24")[1], "UTF-8")
                            urlList.add(playurl)
                        } catch (e: Exception) {
                            return@forEach
                        }
                    }
                    lineWithPlayerurls.append(index, urlList)
                }
            }

            Pair(line, jiList)
        }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl =
        withContext(Dispatchers.IO) {
            if (line >= lineWithPlayerurls.size()) {
                VideoUrl("$BASE_URL/?m=vod-detail-id-$id.html", true)
            } else {
                val url = lineWithPlayerurls[line].takeIf { ji < it.size }

                if (url == null) {
                    VideoUrl("$BASE_URL/?m=vod-detail-id-$id.html", true)
                } else {
                    var playurl = lineWithPlayerurls[line][ji]
                    if (!playurl.contains(".mp4") || !playurl.contains(".m3u8")) {
                        try {
                            playurl = OkhttpUtil.getResponseData(playurl).run {
                                val urlText = substring(indexOf("main")).split(";")[0].run {
                                    substring(this.indexOf("\"") + 1, this.lastIndexOf("\""))
                                }
                                val uri = URI(playurl)

                                "${uri.scheme}://${uri.host}$urlText"
                            }
                        } catch (e: Exception) {
                            VideoUrl("$BASE_URL/?m=vod-detail-id-$id.html", true)
                        }
                    }
                    VideoUrl(playurl, false)
                }
            }
        }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            if (playerDetailDocument == null) {
                val url = "$BASE_URL?m=vod-detail-id-$id.html"
                playerDetailDocument = Jsoup.parse(OkhttpUtil.getResponseData(url))
            }
            val elements = playerDetailDocument!!.getElementsByClass("QZ0baptH")
            val bangumis = ArrayList<Bangumi>()
            elements.forEach { bangumisElements ->
                bangumisElements.children().forEach { bangumiElement ->
                    val bangumiId = parserid(bangumiElement.get_a_tags()[0].attrHref())!!
                    val name = bangumiElement.getElementsByTag("h3").text()
                    val cover = if (bangumiElement.get_img_tags()[0].attrSrc().contains("http")) {
                        bangumiElement.get_img_tags()[0].attrSrc()
                    } else {
                        "$BASE_URL${bangumiElement.get_img_tags()[0].attrSrc()}"
                    }
                    val ji = bangumiElement.get_p_tags()[0].text()
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
        callback: LoadInitialCallback<Int, Bangumi>) {

        val url = "$BASE_URL/index.php?m=vod-search-pg-1-wd-$encodeSearchWord.html"

        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val result = parserBangumiFromHtml(document)
        callback.onResult(result, null, 2)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {
        val page = params.key
        val url = "$BASE_URL/index.php?m=vod-search-pg-$page-wd-$encodeSearchWord.html"
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val result = parserBangumiFromHtml(document)

        if (result.isEmpty()) {
            callback.onResult(result, null)
        } else {
            callback.onResult(result, page + 1)
        }
    }

    private fun parserBangumiFromHtml(document: Document): List<Bangumi> {
        val bangumisElement = document.getElementsByClass("d-vod-list")[0]
        if (bangumisElement.children().size == 0) return emptyList()
        val bangumis = ArrayList<Bangumi>()
        bangumisElement.children().forEach { bangumiElement ->
            val bangumiMessage = bangumiElement.get_a_tags()[0]
            val id = parserid(bangumiMessage.attrHref()) ?: ""
            val imgTag = bangumiMessage.get_img_tags()[0]
            val cover = if (imgTag.attrSrc().contains("http")) {
                imgTag.attrSrc()
            } else {
                "$BASE_URL${imgTag.attrSrc()}"
            }
            val name = imgTag.attrAlt()
            val ji = bangumiMessage.getElementsByClass("text")[0].text()
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
