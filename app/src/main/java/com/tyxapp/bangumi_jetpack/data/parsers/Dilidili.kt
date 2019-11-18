package com.tyxapp.bangumi_jetpack.data.parsers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.main.home.adapter.BANNER
import com.tyxapp.bangumi_jetpack.utilities.*
import kotlinx.coroutines.*
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import org.jetbrains.anko.collections.forEachWithIndex
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.Exception

private const val BASE_URL_PC = "http://www.dilidili.one"
private const val BASE_URL_PHONE = "http://m.dilidili.one"

class Dilidili : IHomePageParser, IPlayerVideoParser, IsearchParser {

    private var pcHomePageDocument: Document? = null
    private var categoryWithUrl: MutableMap<String, String>? = null
    private val playerHtmlUrls by lazy(LazyThreadSafetyMode.NONE) { ArrayList<String>() }
    private var detailDocument: Document? = null

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        initDetailDocument(id)
        val parentElement = detailDocument!!.getElementsByClass("detail con24 clear")[0]
        val cover = parentElement.get_img_tags().getOrNull(0)?.attrSrc() ?: ""
        val name = parentElement.getElementsByTag("h1").text()
        val DLtag = parentElement.getElementsByClass("d_label")
        val niandai = DLtag.getOrNull(1)?.get_a_tags()?.text() ?: ""
        val type = DLtag.getOrNull(2)?.run {
            buildString {
                this@run.get_a_tags().forEach {
                    append(it.text())
                    append(" ")
                }
            }
        } ?: ""
        val ji = DLtag.getOrNull(3)?.text() ?: ""
        val intor = detailDocument!!.getElementsByClass("d_label2")[2].text()
        BangumiDetail(
            id = id,
            name = name,
            source = BangumiSource.DiliDili,
            intro = intor,
            jiTotal = ji,
            cover = cover,
            type = type,
            niandai = niandai
        )
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            initDetailDocument(id)
            val jiList = ArrayList<JiItem>()
            detailDocument!!.getElementsByClass("swiper-slide").getOrNull(0)
                ?.getElementsByClass("clear")?.getOrNull(0)
                ?.children()?.forEachWithIndex { index, jiElement ->
                    if (playerHtmlUrls.size == index) {
                        playerHtmlUrls.add(jiElement.get_a_tags()[0].attrHref())
                    }
                    val name = jiElement.getElementsByTag("span").text()
                    jiList.add(JiItem(name))
                } ?: return@withContext Pair(0, emptyList<JiItem>())
            Pair(1, jiList)
        }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl =
        withContext(Dispatchers.IO) {
            try {
                val url = playerHtmlUrls[ji]
                val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
                val playerUrl = document.getElementsByTag("iframe")[0].attrSrc().split("=")
                    .run { get(size - 1) }
                if (playerUrl.contains(".html")) throw Exception()
                VideoUrl(playerUrl)
            } catch (e: Exception) {
                VideoUrl("$BASE_URL_PC/anime/$id/", true)
            }
        }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            initDetailDocument(id)
            val bangumis = ArrayList<Bangumi>()
            detailDocument!!.getElementsByClass("m_pic clear")[0].children()
                .forEach { bangumiElement ->
                    val name = bangumiElement.get_p_tags()[0].text()
                    val bangumiId = parserId(bangumiElement.get_a_tags()[0].attrHref())
                    val cover = bangumiElement.get_img_tags()[0].attrSrc()
                    bangumis.add(Bangumi(bangumiId, BangumiSource.DiliDili, name, cover))
                }
            bangumis
        }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? {
        return null
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factor = DiliSearchResultDataSourceFactor(searchWord)
        val pagelist = LivePagedListBuilder(factor, 8).build()
        return Listing(
            liveDataPagelist = pagelist,
            initialLoad = factor.dataSourceLiveData.switchMap { it.initialLoadLiveData },
            netWordState = factor.dataSourceLiveData.switchMap { it.netWordState },
            retry = { factor.dataSourceLiveData.value?.retry() }
        )
    }

    /***************************************视频播放***************************************/

    override suspend fun getHomeBangumis(): Map<String, List<Bangumi>> = coroutineScope {
        pcHomePageDocument = null //刷新数据
        initPcHomePageDocument()
        val asyncArray = arrayOf(
            async { parserBanner() },
            async { parserUpdateBangmi(pcHomePageDocument!!) },
            async { seasonBangumiParser() },
            async { randomBangumiParser() }
        )
        val map = LinkedHashMap<String, List<Bangumi>>()
        asyncArray.forEach {
            val pair = it.await()
            map[pair.first] = pair.second
        }
        map
    }


    /**
     * 轮播图解析
     *
     */
    private suspend fun parserBanner(): Pair<String, List<Bangumi>> =
        withContext(Dispatchers.Default) {
            val document = Jsoup.parse(OkhttpUtil.getResponseData(BASE_URL_PHONE))
            val bangumis = ArrayList<Bangumi>()
            document.getElementsByClass("swiper-wrapper")[0].children().forEach { lunboElement ->
                if (!lunboElement.attrHref().contains("anime")) return@forEach //去广告
                val name = lunboElement.get_img_tags()[0].attrAlt()
                val id = parserId(lunboElement.attrHref())
                val cover = lunboElement.get_img_tags()[0].attrSrc()
                bangumis.add(Bangumi(id, BangumiSource.DiliDili, name, cover))
            }
            Pair(BANNER, bangumis)
        }

    /**
     *解析最新更新
     *
     */
    private suspend fun parserUpdateBangmi(document: Document): Pair<String, List<Bangumi>> =
        withContext(Dispatchers.IO) {
            val deferredIds = ArrayList<Deferred<String>>()
            val bangumis = ArrayList<Bangumi>()
            document.getElementsByClass("book article")[0].children()
                .run {
                    if (document == pcHomePageDocument) {
                        take(5)
                    } else {
                        this
                    }
                }
                .forEach { bangumiElement ->
                    deferredIds.add(async { getId(bangumiElement.attrHref()) })
                    bangumis.add(parserbangumi(bangumiElement))
                }
            bangumis.forEachWithIndex { index, bangumi ->
                bangumi.id = deferredIds[index].await()
            }
            Pair("最新更新", bangumis)
        }

    private suspend fun getId(url: String): String = withContext(Dispatchers.IO) {
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val id = document.getElementById("link").get_a_tags()[1].attrHref()
        parserId(id)
    }

    /**
     * 主页的番剧节点解析
     *
     */
    private fun parserbangumi(bangumiElement: Element): Bangumi {
        val id = parserId(bangumiElement.attrHref())
        val name = bangumiElement.get_p_tags()[0].text()
        val ji = bangumiElement.get_p_tags().getOrNull(1)?.text() ?: ""
        val cover = bangumiElement.getElementsByClass("coverImg").attr("style").run {
            substring(lastIndexOf("(") + 1, lastIndexOf(")"))
        }
        return Bangumi(id, BangumiSource.DiliDili, name, cover, ji)
    }

    /**
     * 随机推荐番剧解析
     *
     */
    private suspend fun randomBangumiParser(): Pair<String, List<Bangumi>> =
        withContext(Dispatchers.Default) {
            val bangumis = ArrayList<Bangumi>()
            pcHomePageDocument!!.getElementsByClass("book suijituijian")[0].children()
                .forEach { bangumiElement ->
                    bangumis.add(parserbangumi(bangumiElement))
                }
            Pair("随机番剧推荐", bangumis)
        }

    /**
     * 本季新番解析
     *
     */
    private suspend fun seasonBangumiParser(): Pair<String, List<Bangumi>> =
        withContext(Dispatchers.Default) {
            val aTag = pcHomePageDocument!!.getElementsByClass("menu")[0].get_a_tags()[1]
            val text = aTag.text()
            val url = "$BASE_URL_PC${aTag.attrHref()}"
            val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
            val bangumis = ArrayList<Bangumi>()
            document.getElementsByClass("anime_list")[0]
                .children()
                .take(6)
                .forEach { bangumiElement ->

                    val aTags = bangumiElement.get_a_tags()
                    val id = parserId(aTags[0].attrHref())
                    val name = aTags[1].text()
                    val cover = bangumiElement.get_img_tags()[0].attrSrc()
                    val ji =
                        bangumiElement.getElementsByAttributeValue(
                            "style",
                            "color:#F00"
                        )[0].parent()
                            .text()

                    bangumis.add(Bangumi(id, BangumiSource.DiliDili, name, cover, ji))
                }
            Pair(text, bangumis)
        }

    override suspend fun getCategorItems(): List<CategorItem> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL_PHONE/fenlei.html"
        val categorItems = ArrayList<CategorItem>()
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        if (categoryWithUrl == null) {
            categoryWithUrl = HashMap()
        }
        document.getElementsByClass("classification_list")[0]
            .getElementsByTag("ul")[0].children().forEach { categorItemElement ->

            val cover = categorItemElement.get_img_tags()[0].attrSrc()
            val name = categorItemElement.get_p_tags()[0].text()
            categorItems.add(CategorItem(cover, name))

            if (categoryWithUrl?.get(name) == null) {
                categoryWithUrl?.put(name, categorItemElement.get_a_tags()[0].attrHref())
            }
        }
        categorItems
    }

    override fun getCategoryBangumis(category: String): Listing<Bangumi> {
        return if (category == "最新更新") {
            val factory = UpdateBnagumisDataSourceFactory()
            val pageList = LivePagedListBuilder(factory, 10).build()
            Listing(
                liveDataPagelist = pageList,
                initialLoad = factory.dataSourchLiveData.switchMap { it.initialLoadLiveData },
                netWordState = factory.dataSourchLiveData.switchMap { it.netWordState },
                retry = { factory.dataSourchLiveData.value?.retry() }
            )
        } else {
            val url = if (category.contains("月")) { //新番
                val nUrl =
                    pcHomePageDocument!!.getElementsByClass("menu")[0].get_a_tags()[1].attrHref()
                "$BASE_URL_PHONE$nUrl"
            } else {
                categoryWithUrl!![category]!!
            }
            val factory = CategoryResultDataSourceFactory(url)
            val pageList = LivePagedListBuilder(factory, 20).build()
            Listing(
                liveDataPagelist = pageList,
                initialLoad = factory.dataSourceLiveData.switchMap { it.initialLoadLiveData },
                netWordState = factory.dataSourceLiveData.switchMap { it.netWordState },
                retry = { factory.dataSourceLiveData.value!!.retry() }
            )
        }

    }

    private inner class UpdateBnagumisDataSource : PageResultDataSourch<Int>(null) {
        override fun initialLoad(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Int, Bangumi>
        ) {
            LOGI("nvnb")
            val document = Jsoup.parse(OkhttpUtil.getResponseData("$BASE_URL_PC/zxgx/"))
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val result = parserUpdateBangmi(document).second
                    callback.onResult(result, null, null)
                    initialLoadLiveData.postValue(
                        InitialLoad(
                            NetWordState.SUCCESS,
                            result.isEmpty()
                        )
                    )
                } catch (e: Exception) {
                    initialLoadLiveData.postValue(InitialLoad(NetWordState.error(e.toString())))
                }
            }
        }

        override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

        }

    }

    private inner class UpdateBnagumisDataSourceFactory : DataSource.Factory<Int, Bangumi>() {
        val dataSourchLiveData = MutableLiveData<UpdateBnagumisDataSource>()
        override fun create(): DataSource<Int, Bangumi> {
            return UpdateBnagumisDataSource().apply { dataSourchLiveData.postValue(this) }
        }
    }

    override suspend fun getBangumiTimeTable(): List<List<Bangumi>> = withContext(Dispatchers.IO) {
        initPcHomePageDocument()
        val weekBangumisElement = pcHomePageDocument!!.getElementsByClass("book small")
        val weekBangumis = ArrayList<List<Bangumi>>()
        weekBangumisElement.forEach { dayBangumisElement ->
            val dayBangumis = ArrayList<Bangumi>()
            dayBangumisElement.children().forEach { bagnumiElement ->
                val id = parserId(bagnumiElement.attrHref())
                val cover = bagnumiElement.get_img_tags()[0].attrSrc()
                val name = bagnumiElement.get_p_tags()[0].text()
                dayBangumis.add(Bangumi(id, BangumiSource.DiliDili, name, cover))
            }
            weekBangumis.add(dayBangumis)
        }
        weekBangumis
    }


    private suspend fun initPcHomePageDocument() {
        withContext(Dispatchers.IO) {
            synchronized(Dilidili::class.java) {
                if (pcHomePageDocument == null) {
                    pcHomePageDocument = Jsoup.parse(OkhttpUtil.getResponseData(BASE_URL_PC))
                }
            }
        }
    }

    private fun initDetailDocument(id: String) {
        synchronized(Dilidili::class.java) {
            if (detailDocument == null) {
                val url = "$BASE_URL_PC/anime/$id/"
                detailDocument = Jsoup.parse(OkhttpUtil.getResponseData(url))
            }
        }
    }
}

private fun parserId(idString: String): String {
    return if (idString[idString.length - 1] == '/') {
        idString.split("/").run { get(size - 2) }
    } else {
        idString.split("/").run { get(size - 1) }
    }
}

private class DiliSearchResultDataSource(
    searchWord: String
) : PageResultDataSourch<String>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Bangumi>
    ) {

        val url = "$BASE_URL_PC/search.php?q=$encodeSearchWord"
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val nextUrl = getNextPageUrl(document)
        val result: List<Bangumi> = parserSearchResult(document)
        val isEmpty = result.isEmpty()
        callback.onResult(result, null, nextUrl)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, isEmpty))
    }

    private fun getNextPageUrl(document: Document): String? {
        return document.getElementsByClass("pagelist")
            .getOrNull(0)
            ?.run {
                val hasNext = get_a_tags().getOrNull(0)?.text()?.equals("下一页") ?: false
                if (hasNext) {
                    "$BASE_URL_PC${get_a_tags()[0].attrHref()}"
                } else {
                    null
                }
            }
    }

    private fun parserSearchResult(document: Document): List<Bangumi> {
        val bangumis = ArrayList<Bangumi>()
        document.getElementsByClass("col-sm-6 hang").forEach { bangumiElement ->
            val aTag = bangumiElement.get_a_tags().getOrNull(1)
            val id = aTag?.run { parserId(this.attrHref()) } ?: return@forEach
            val name = aTag.getElementsByTag("h2")?.text() ?: ""

            val cover = bangumiElement.get_img_tags().getOrNull(0)?.attrSrc() ?: ""
            bangumis.add(Bangumi(id, BangumiSource.DiliDili, name, cover))
        }
        return bangumis
    }

    override fun afterload(params: LoadParams<String>, callback: LoadCallback<String, Bangumi>) {
        val document = Jsoup.parse(params.key)
        val nextUrl = getNextPageUrl(document)
        val result = parserSearchResult(document)
        callback.onResult(result, nextUrl)
    }

}

private class DiliSearchResultDataSourceFactor(
    private val searchWord: String
): DataSource.Factory<String, Bangumi>() {
    val dataSourceLiveData = MutableLiveData<DiliSearchResultDataSource>()

    override fun create(): DataSource<String, Bangumi> {
        return DiliSearchResultDataSource(searchWord).apply {
            dataSourceLiveData.postValue(this)
        }
    }
}

private class CategoryResultDataSource(
    private val categoryUrl: String
) : PageResultDataSourch<Int>(null) {
    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>
    ) {

        val document = Jsoup.parse(OkhttpUtil.getResponseData(categoryUrl))
        val bangumis = ArrayList<Bangumi>()
        document.getElementById("episode_list").children().forEach { bangumiElement ->
            val id = parserId(bangumiElement.child(0).attrHref())
            val name = bangumiElement.getElementsByClass("ac").text()
            val cover = bangumiElement.getElementsByClass("episodeImg")
                .attr("style").run {
                    substring(indexOf("(") + 1)
                }
            val kandian = bangumiElement.getElementsByClass("kandian")
                .getOrNull(0)?.getElementsByTag("span")?.get(0)?.text() ?: ""
            val type = bangumiElement.getElementsByClass("kandian")
                .getOrNull(1)?.getElementsByTag("span")?.get(0)?.text() ?: ""
            val jianjie = bangumiElement.getElementsByClass("jianjie").getOrNull(0)
                ?.getElementsByTag("span")?.get(0)?.text() ?: ""
            bangumis.add(
                DiliBangumi(
                    id = id,
                    source = BangumiSource.DiliDili,
                    name = name,
                    cover = cover,
                    intro = jianjie,
                    kandian = kandian,
                    type = type
                )
            )
        }
        callback.onResult(bangumis, null, null)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, bangumis.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

    }

}

private class CategoryResultDataSourceFactory(
    private val categoryUrl: String
) : DataSource.Factory<Int, Bangumi>() {
    val dataSourceLiveData = MutableLiveData<CategoryResultDataSource>()

    override fun create(): DataSource<Int, Bangumi> {
        return CategoryResultDataSource(categoryUrl).apply {
            dataSourceLiveData.postValue(this)
        }
    }

}