package com.tyxapp.bangumi_jetpack.data.parsers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.player.danmakuparser.BiliDanmukuParser
import com.tyxapp.bangumi_jetpack.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import okhttp3.FormBody
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private const val BASEURL_PC = "http://www.silisili.me"
private const val DANMU_URL = "http://27.124.39.40"

class Silisili : IsearchParser, IPlayerVideoParser {
    private var detailDocument: Document? = null

    private fun initDetailDocument(id: String) {
        synchronized(Silisili::class.java) {
            if (detailDocument == null) {
                val url = "$BASEURL_PC/anime/$id.html"
                detailDocument = Jsoup.parse(OkhttpUtil.getResponseData(url))
            }
        }
    }

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        initDetailDocument(id)
        val detailElement = detailDocument!!.getElementsByClass("detail con24 clear")[0]
        val cover = detailElement.get_img_tags()[0].attrSrc().run {
            if (contains("http")) {
                this
            } else {
                "$BASEURL_PC$this"
            }
        }

        val name = detailElement.getElementsByTag("h1")[0].text()
        val DLTag = detailElement.getElementsByClass("d_label")
        val niandai = DLTag.getOrNull(1)?.get_a_tags()?.text() ?: ""
        val type = DLTag.getOrNull(2)?.text() ?: ""
        val ji = DLTag.getOrNull(3)?.text() ?: ""
        val intro =
            detailElement.getElementsByClass("d_label2").getOrNull(1)?.text()
                ?: ""
        BangumiDetail(
            id = id,
            source = BangumiSource.SiliSili,
            name = name,
            cover = cover,
            jiTotal = ji,
            niandai = niandai,
            type = type,
            intro = intro
        )
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> = withContext(Dispatchers.IO) {
        initDetailDocument(id)
        val jiListElement = detailDocument!!.getElementById("show").getElementsByClass("clear")[0]
        val jiList = ArrayList<JiItem>()
        jiListElement.children().forEach {
            val name = it.getElementsByTag("span").text()
            jiList.add(JiItem(name))
        }
        Pair(1, jiList)
    }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl = withContext(Dispatchers.IO) {
        val url = "$BASEURL_PC/play/$id-${ji + 1}.html"
        var document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val videoUrl = document.getElementsByTag("iframe")[0].attrSrc()

        return@withContext if (videoUrl.contains("?http")) {
            VideoUrl(videoUrl.split("?")[1].trim())
        } else {
            document = Jsoup.parse(OkhttpUtil.getResponseData(videoUrl))
            val videoUrlElement = document.getElementsByTag("source")
            if (videoUrlElement.isEmpty()) {
                try {
                    val playerUrl = document.getElementsByTag("script")[2]
                        .toString()
                        .split(";")[0]
                        .run { substring(indexOf("'") + 1, lastIndexOf("'")) }
                    VideoUrl(playerUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                    VideoUrl(url, true)
                }
            } else {
                VideoUrl(videoUrlElement[0].attrSrc())
            }
        }
    }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> = withContext(Dispatchers.IO) {
        initDetailDocument(id)
        val bangumis = ArrayList<Bangumi>()
        val bangumisElement = detailDocument!!.getElementsByClass("m_pic clear")
            .getOrNull(0)
            ?: return@withContext emptyList<Bangumi>()

        bangumisElement.children().forEach { bangumiElement ->
            val name = bangumiElement.get_p_tags()
                .getOrNull(0)
                ?.text()
                ?: ""

            val bangumiId = getHtmlBangumiId(bangumiElement.get_a_tags()[0].attrHref())!!
            val cover = bangumiElement.get_img_tags().getOrNull(0)
                ?.attrSrc()?.run {
                    if (contains("http")) {
                        this
                    } else {
                        "$BASEURL_PC$this"
                    }
                }
                ?: ""
            bangumis.add(Bangumi(bangumiId, BangumiSource.SiliSili, name, cover))
        }
        bangumis
    }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? = withContext(Dispatchers.IO) {
        val url = "$DANMU_URL/danmu/dm/$id/$id-$ji.php"
        val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
        loader.load(OkhttpUtil.getResponseBody(url).byteStream())
        BiliDanmukuParser().apply { load(loader.dataSource) }
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factory = SiliSiliSearchResultDataSourceFactory(searchWord)
        val pageList = LivePagedListBuilder(factory, 8).build()
        return Listing(
            liveDataPagelist = pageList,
            netWordState = factory.dataSourceLiveData.switchMap { it.netWordState },
            initialLoad = factory.dataSourceLiveData.switchMap { it.initialLoadLiveData },
            retry = { factory.dataSourceLiveData.value!!.retry() }
        )
    }

}

private class SiliSiliSearchResultDataSource(
    private val searchWord: String
) : PageResultDataSourch<String>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Bangumi>
    ) {
        val url = "$BASEURL_PC/e/search/index.php"
        val formBody = FormBody.Builder()
            .add("show", "title,ftitle,zz")
            .add("tbname", "movie")
            .add("tempid", "1")
            .add("keyboard", searchWord)
            .build()

        val request = Request.Builder()
            .post(formBody)
            .url(url)
            .build()

        val html = OkhttpUtil.getResponseData(request)
        val document = Jsoup.parse(html)
        val result = parserBangumisFromHtml(document)

        callback.onResult(result, null, findNextPageurl(document))
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    private fun findNextPageurl(document: Document): String? {
        val pageTag = document.getElementsByClass("page")[0]
        return if (pageTag.children().size == 0) {
            null
        } else {
            val aTags = pageTag.get_a_tags()
            var pageUrl: String? = null
            for (element in aTags) {
                if (element.text().contains("下一页")) {
                    pageUrl = "$BASEURL_PC${element.attrHref()}"
                    break
                }
            }
            pageUrl
        }
    }

    private fun parserBangumisFromHtml(document: Document): List<Bangumi> {
        val bangumis = ArrayList<Bangumi>()
        val bangumisElement = document.getElementsByClass("anime_list")[0]
        bangumisElement.children().forEach { bangumiElement ->
            val aTag = bangumiElement.get_a_tags()[1]
            val id = getHtmlBangumiId(aTag.attrHref())!!
            val name = aTag.text()
            val cover = bangumiElement.get_img_tags().takeIf { it.isNotEmpty() }?.run {
                get(0)?.attrSrc()?.run {
                    if (this.contains("http")) {
                        this
                    } else {
                        "$BASEURL_PC$this"
                    }
                }
            } ?: ""

            val ji = bangumiElement.getElementsByAttributeValue(
                "style",
                "color:#F00"
            ).getOrNull(0)?.parent()?.text() ?: ""

            bangumis.add(Bangumi(id, BangumiSource.SiliSili, name, cover, ji))
        }
        return bangumis
    }

    override fun afterload(params: LoadParams<String>, callback: LoadCallback<String, Bangumi>) {
        val document = Jsoup.parse(OkhttpUtil.getResponseData(params.key))
        val result = parserBangumisFromHtml(document)
        callback.onResult(result, findNextPageurl(document))
    }

}

private class SiliSiliSearchResultDataSourceFactory(
    private val searchWord: String
) : DataSource.Factory<String, Bangumi>() {
    val dataSourceLiveData = MutableLiveData<SiliSiliSearchResultDataSource>()
    override fun create(): DataSource<String, Bangumi> {
        return SiliSiliSearchResultDataSource(searchWord).apply {
            dataSourceLiveData.postValue(this)
        }
    }

}

