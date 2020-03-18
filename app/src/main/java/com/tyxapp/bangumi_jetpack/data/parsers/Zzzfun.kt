package com.tyxapp.bangumi_jetpack.data.parsers

import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.main.home.adapter.BANNER
import com.tyxapp.bangumi_jetpack.player.danmakuparser.BiliDanmukuParser
import com.tyxapp.bangumi_jetpack.utilities.*
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil.getResponseData
import kotlinx.coroutines.*
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder

private const val GET_BASE_URL = "http://alicdn-1259251677.file.myqcloud.com/android.txt"
private var BASE_URL = "http://service-agbhuggw-1259251677.gz.apigw.tencentcs.com/android"
private const val SEARCH_URL = "http://111.230.89.165:8099/api.php/provvde/vod/?ac=list&wd="
private const val PC_URL = "http://www.zzzfun.com"

class Zzzfun : IHomePageParser, IsearchParser, IPlayerVideoParser {

    private val linkVideoUrls: SparseArray<List<String>> by lazy(LazyThreadSafetyMode.NONE) {
        SparseArray<List<String>>()
    }

    constructor() {
        GlobalScope.launch(Dispatchers.IO) {
            BASE_URL = OkhttpUtil.getResponseData(GET_BASE_URL)
        }
    }

    override suspend fun getHomeBangumis(): Map<String, List<Bangumi>> =
        withContext(Dispatchers.IO) {
            val bangumiGroup = LinkedHashMap<String, List<Bangumi>>()
            val homeBangumiGroup = async { parserHomeBangumis() }
            val banners = async { parserBanner() }
            bangumiGroup.run {
                putAll(homeBangumiGroup.await())
                plus(banners.await())
            }
        }

    /**
     * 解析主页番剧
     *
     * @return
     */
    private fun parserHomeBangumis(): Map<String, List<Bangumi>> {
        val url = "$BASE_URL/home/tj.php "
        val homeBangumiGroup = LinkedHashMap<String, List<Bangumi>>()
        val jsonArray = JSONObject(getResponseData(url)).getJSONArray("data")
        jsonArray.forEach { groups ->
            val jsonBangumis = groups.getJSONArray("content")
            val bangumis = ArrayList<Bangumi>()
            jsonBangumis.forEach {
                bangumis.add(parserBnagumi(it))
            }

            val title = groups.getString("title")
            homeBangumiGroup[title] = bangumis
        }
        return homeBangumiGroup
    }



    /**
     * 解析头部轮播
     *
     */
    private fun parserBanner() : Pair<String, List<Bangumi>> {
        val url = "$BASE_URL/home/home?v=pic&vcode=103"
        val jsonArray = JSONObject(getResponseData(url)).getJSONArray("data")
        val bangumis = ArrayList<Bangumi>()
        jsonArray.forEach {
            val id = it.getString("bannerId")
            if (id == "ad") return@forEach
            val cover = it.getString("bannerImg")
            val name = it.getString("bannerName")
            bangumis.add(Bangumi(id, BangumiSource.Zzzfun, name, cover))
        }
        return BANNER to bangumis
    }


    override suspend fun getCategorItems(): List<CategorItem> = withContext(Dispatchers.IO) {
        val list = ArrayList<CategorItem>()
        val url = "$BASE_URL/type/type?userid="
        JSONObject(getResponseData(url)).getJSONArray("data").forEach {
            val name = it.getString("typename")
            val cover = it.getString("typepic")
            list.add(CategorItem(cover, name))
        }
        list
    }

    override fun getCategoryBangumis(category: String): Listing<CategoryBangumi> {
        val sourceFactor = CategoryPageDataSourceFactor(category)


        val liveDataPagelist = LivePagedListBuilder(sourceFactor, 10).build()

        return Listing<CategoryBangumi>(
            liveDataPagelist = liveDataPagelist,
            netWordState = sourceFactor.sourceLiveData.switchMap { it.netWordState },
            retry = { sourceFactor.sourceLiveData.value?.retry() },
            initialLoad = sourceFactor.sourceLiveData.switchMap { it.initialLoadLiveData }
        )

    }


    /**
     *  {
    "videoId": "1758",
    "videoImg": "https://img3.doubanio.com/view/photo/s_ratio_poster/public/p2566344205.jpg",
    "videoName": "巴比伦",
    "time": "2020-01-21",
    "ji": "第11话"
    }
     */
    override suspend fun getBangumiTimeTable(): List<List<Bangumi>> = withContext(Dispatchers.IO) {
        val baseDayUrl = "$BASE_URL/week?day="
        val weekBnagumis = ArrayList<List<Bangumi>>()
        for (day in 1..7) {
            val dayBnagumis = ArrayList<Bangumi>()
            JSONObject(getResponseData("$baseDayUrl$day")).getJSONArray("data").forEach {
                val id = it.getString("videoId")
                val cover = it.getString("videoImg")
                val name = it.getString("videoName")
                val ji = it.getString("ji")
                LOGI(name)
                dayBnagumis.add(Bangumi(id, BangumiSource.Zzzfun, name, cover, ji))
            }
            weekBnagumis.add(dayBnagumis)
        }
        weekBnagumis
    }

    /*********************************搜索结果******************************************************/

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factor = SearchResultDataSourceFactor(searchWord)

        val livePagedList = LivePagedListBuilder(factor, 10).build()

        return Listing(
            liveDataPagelist = livePagedList,
            netWordState = factor.searchResultDataSource.switchMap { it.netWordState },
            retry = { factor.searchResultDataSource.value?.retry() },
            initialLoad = factor.searchResultDataSource.switchMap { it.initialLoadLiveData }
        )
    }

    /*********************************视频播放解析******************************************************/

    override suspend fun getBangumiDetail(id: String): BangumiDetail = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/video/list_ios?videoId=$id"
        val jsonObject = JSONObject(getResponseData(url)).getJSONObject("data")
        val request = Request.Builder().run {
            addHeader("User-Agent", PHONE_REQUEST)
            url("$PC_URL/vod-detail-id-$id.html")
            build()
        }

        val document = Jsoup.parse(getResponseData(request))

        val niandai = document.getElementsByAttributeValue("itemprop", "uploadDate")
            .getOrNull(0)?.attr("content") ?: ""

        val type = jsonObject.getString("videoClass")

        val cast =
            document.getElementsByAttributeValue("itemprop", "actor")
                .getOrNull(0)
                ?.run { this.attr("content").replace(",", "\n") }
                ?: ""

        val intro = document.getElementsByClass("leo-color-e leo-fs-s leo-ellipsis-2")
            .getOrNull(0)?.text() ?: jsonObject.getString("videoDoc")

        val jiTotal =
            document.getElementsByClass("leo-color-a leo-fs-l leo-ellipsis-1")
                .getOrNull(0)
                ?.run { text().split("|")[1].trim() }
                ?: jsonObject.getString("videoremarks")

        val staff =
            document.getElementsByClass("leo-ellipsis-1 leo-fs-s leo-lh-ss")
                .getOrNull(0)?.text() ?: ""

        val cover =
            document.getElementsByClass("leo-lazy leo-radius-s")
                .getOrNull(0)
                ?.run { attr("data-original") }
                ?: jsonObject.getString("videoImg")

        val name =
            document.getElementsByClass("leo-lazy leo-radius-s")
                .getOrNull(0)?.attrAlt() ?: jsonObject.getString("videoName")

        BangumiDetail(
            id = id,
            source = BangumiSource.Zzzfun,
            name = name,
            cover = cover,
            niandai = niandai,
            cast = cast,
            type = type,
            staff = staff,
            jiTotal = jiTotal,
            intro = intro
        )
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/video/list_ios?videoId=$id"
            val jsonObject = JSONObject(getResponseData(url))
            val jiList = ArrayList<JiItem>()
            val lineJaonArray = jsonObject.getJSONObject("data").getJSONArray("videoSets")
            var line = 0
            lineJaonArray.forEach {
                val jiListAndVideoUrls = parserToJiList(it.getJSONArray("list"))
                if (jiList.size != jiListAndVideoUrls.jiList.size) {
                    jiList.clear()
                    jiList.addAll(jiListAndVideoUrls.jiList)
                }
                linkVideoUrls.append(line, jiListAndVideoUrls.playIds)
                line++
            }

            line to jiList
        }

    private fun parserToJiList(jsonArray: JSONArray): JiListAndVideoUrls {
        val list = ArrayList<JiItem>()
        val videoUrls = ArrayList<String>()

        jsonArray.forEach {
            list.add(JiItem("第${it.getString("ji")}话"))
            videoUrls.add(it.getString("playid"))
        }

        return JiListAndVideoUrls(list, videoUrls)
    }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl =
        withContext(Dispatchers.IO) {
            try {
                val playid = linkVideoUrls[line][ji]
                val md5Str = MD5Util.md5(playid + 534697)
                val url = "$BASE_URL/video/play"

                val requestBodyPost = FormBody.Builder()
                    .add("playid", playid)
                    .add("sing", md5Str)
                    .build()
                val request = Request.Builder()
                    .post(requestBodyPost)
                    .url(url)
                    .build()
                val jsonObject = JSONObject(getResponseData(request))
                val playurl = jsonObject.getJSONObject("data").getString("videoplayurl")
                VideoUrl(playurl)
            } catch (e:Exception) {
                VideoUrl("$PC_URL/index.php/vod-detail-id-$id.html", true)
            }
        }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/video/tdmlist?cid=$id-${ji + 1}"
            val iLoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            iLoader.load(OkhttpUtil.getResponseBody(url).byteStream())
            BiliDanmukuParser().apply { load(iLoader.dataSource) }
        }

    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/video/with"
            val jsonArray = JSONObject(getResponseData(url))
                .takeIf { !it.isNull("data") }
                ?.getJSONArray("data")
                ?: return@withContext emptyList<Bangumi>()

            val bangumis = ArrayList<Bangumi>()
            jsonArray.forEach {
                bangumis.add(parserBnagumi(it))
            }
            bangumis
        }


}

/**
 *  {
"videoId": "219",
"videoImg": "http://puui.qpic.cn/fans_admin/0/3_380704150_1578035344262/0",
"videoName": "告白实行委员会剧场版",
"videoremarks": "HD"
}
 */
private fun parserBnagumi(jsonObject: JSONObject) : Bangumi {
    val id = jsonObject.getString("videoId")
    val name = jsonObject.getString("videoName")
    val cover = jsonObject.getString("videoImg")
    val ji = jsonObject.getString("videoremarks")

    return Bangumi(id, BangumiSource.Zzzfun, name, cover, ji)
}

private class ZzzfunSearchResultDataSource(
    private val searchWord: String
) : PageResultDataSourch<Int, Bangumi>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>) {


        val url = "$BASE_URL/search"

        val requestBody = FormBody.Builder()
            .add("userid", "")
            .add("key", searchWord)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val jsonArray = JSONObject(getResponseData(request)).getJSONArray("data").takeIf { it.length() != 0 }
        LOGI(JSONObject(getResponseData(request)).toString())
        if (jsonArray == null) {
            initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, true))
            return callback.onResult(emptyList(), null, null)
        }

        val bangumis = ArrayList<Bangumi>()
        jsonArray.forEach {
            bangumis.add(parserBnagumi(it))
        }
        callback.onResult(bangumis, null, null)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, bangumis.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

    }

}

private data class JiListAndVideoUrls(
    val jiList: List<JiItem>,
    val playIds: List<String>
)

private class SearchResultDataSourceFactor(
    private val searchWord: String
) : DataSource.Factory<Int, Bangumi>() {
    val searchResultDataSource = MutableLiveData<ZzzfunSearchResultDataSource>()

    override fun create(): DataSource<Int, Bangumi> {
        return ZzzfunSearchResultDataSource(searchWord).apply {
            searchResultDataSource.postValue(this)
        }
    }

}

private class CategoryPageDataSource(
    private val category: String
) : PageResultDataSourch<Int, CategoryBangumi>(category) {
    private val bangumiDetailDao = AppDataBase.getInstance().bangumiDetailDao()


    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, CategoryBangumi>) {

        val page = 1
        val result = parserCategoryData(category, page)
        callback.onResult(result, null, page + 1)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, CategoryBangumi>) {
        val page = params.key
        val result: List<CategoryBangumi> = try {
            parserCategoryData(category, page)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
        callback.onResult(result, if (result.isEmpty()) null else page + 1)
    }

    private fun getCategoryTypeId(category: String) : Int = when (category) {
        "电影" -> 39
        "TV剧" -> 40
        "真人版" -> 38
        "国漫" -> 2
        "日漫", "动漫" -> 1
        "剧场版", "剧场" ->3
        "新番" -> 42
        else -> throw IllegalArgumentException("没有这个类型")
    }

    private fun parserCategoryData(category: String, page: Int): List<CategoryBangumi> {
        val url = "$BASE_URL/type/typelist?pg=$page&typeid=${getCategoryTypeId(category)}"

        val jsonObject= JSONObject(getResponseData(url))

        jsonObject.getJSONArray("data").takeIf { it.length() != 0 } ?: return emptyList()

        return jsonObject.getJSONArray("data").run {
            val bangumis = ArrayList<CategoryBangumi>()
            this.forEach {
                val name = it.getString("videoName")
                val cover = it.getString("videoImg")
                val id = it.getString("videoId")
                val isFollow = bangumiDetailDao.isFollowingBangumi(id, BangumiSource.Zzzfun.name)
                bangumis.add(CategoryBangumi(id, name, BangumiSource.Zzzfun, cover, isFollow = isFollow))
            }
            bangumis
        }
    }
}

private class CategoryPageDataSourceFactor(
    private val category: String
) : DataSource.Factory<Int, CategoryBangumi>() {
    val sourceLiveData = MutableLiveData<CategoryPageDataSource>()

    override fun create(): DataSource<Int, CategoryBangumi> = CategoryPageDataSource(category).apply {
        sourceLiveData.postValue(this)
    }
}