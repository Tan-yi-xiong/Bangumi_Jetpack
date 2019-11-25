package com.tyxapp.bangumi_jetpack.data.parsers

import android.util.SparseArray
import androidx.core.util.isEmpty
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.main.home.adapter.BANNER
import com.tyxapp.bangumi_jetpack.player.danmakuparser.ZzzFunDanmakuParser
import com.tyxapp.bangumi_jetpack.utilities.*
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil.getResponseData
import kotlinx.coroutines.*
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

private const val BASE_URL = "http://111.230.89.165:8089/zapi"
private const val SEARCH_URL = "http://111.230.89.165:8099/api.php/provvde/vod/?ac=list&wd="
private const val PC_URL = "http://www.zzzfun.com"

class Zzzfun : IHomePageParser, IsearchParser, IPlayerVideoParser {

    private val categorItemName by lazy(LazyThreadSafetyMode.NONE) {
        BangumiApp.getContext()
            .resources.getStringArray(R.array.zzzfun_categor_name)
    }
    private val categorItemImages: IntArray by lazy(LazyThreadSafetyMode.NONE) {
        intArrayOf(
            R.drawable.zzzfun_category_movie,
            R.drawable.zzzfun_category_dianshiju,
            R.drawable.zzzfun_category_zhenren,
            R.drawable.zzzfun_category_season_spring,
            R.drawable.zzzfun_category_season_summer,
            R.drawable.zzzfun_category_season_autumn,
            R.drawable.zzzfun_category_season_winter,
            R.drawable.zzzfun_category_guocan,
            R.drawable.zzzfun_category_teleplay,
            R.drawable.zzzfun_category_japan_bangumi
        )
    }

    private val linkVideoUrls: SparseArray<List<String>> by lazy(LazyThreadSafetyMode.NONE) {
        SparseArray<List<String>>()
    }

    /**
     * zzzfun主页分为6部分, URL为 /type/home.php?t=1
     * t的值分别为9(头部轮播), 42, 1-4,
     */
    override suspend fun getHomeBangumis(): Map<String, List<Bangumi>> =
        withContext(Dispatchers.IO) {
            val titles = BangumiApp.getContext()
                .resources.getStringArray(R.array.zzfun_title)
            val jobList = ArrayList<Deferred<List<Bangumi>>>()
            val bangumiGroup = LinkedHashMap<String, List<Bangumi>>()

            titles.forEachIndexed { index, title ->
                var position = index
                if (index == 0) {
                    position = 42
                } else if (index == 5) {
                    position = 9
                }

                jobList.add(async { parserHomeBangumis(position, title) })
            }

            jobList.forEachIndexed { postion, bangumis ->
                val key = titles[postion]
                bangumiGroup[key] = bangumis.await()
            }
            bangumiGroup
        }

    private fun parserHomeBangumis(position: Int, title: String): List<Bangumi> {
        val url = "$BASE_URL/type/home.php?t=$position"
        val jsonObject = JSONObject(getResponseData(url))
        return jsonObject.run {
            if (isNull("result")) throw NullPointerException("zzzfunHome $position 结果为空")
            val list = ArrayList<Bangumi>()
            jsonObject.getJSONArray("result").forEach {

                //轮播图封面是"img"
                if (title == BANNER) {
                    it.replace("pic", "")
                }
                list.add(parserToBangumi(it))
            }
            list
        }
    }

    /**
    "hits": "220",
    "id": "1327",
    "img": "",
    "name": "全职法师第3季",
    "pic": "http://ws3.sinaimg.cn/large/006AdpFDgy1g0xq8n82qzj307i0amaai.jpg",
    "remarks": "",
    "serial": "",
    "total": "12"
     */
    private fun parserToBangumi(jsonObject: JSONObject): Bangumi = jsonObject.run {
        val id = getString("id")
        val name = getString("name")
        val cover = when {
            getString("pic").isEmpty() -> getString("img")
            else -> getString("pic")
        }
        val jiTotal = when {
            getString("remarks").isNotEmpty() -> getString("remarks")
            getString("serial").isNotEmpty() -> "更新至${getString("serial")}话"
            else -> "全${getString("total")}话"
        }
        Bangumi(id, BangumiSource.Zzzfun, name, cover, jiTotal)
    }

    override suspend fun getCategorItems(): List<CategorItem> = withContext(Dispatchers.IO) {
        val list = ArrayList<CategorItem>()
        categorItemImages.forEachIndexed { index, i ->
            val categorItem = CategorItem(i, categorItemName[index])
            list.add(categorItem)
        }
        list
    }

    override fun getCategoryBangumis(category: String): Listing<Bangumi> {
        val sourceFactor = CategoryPageDataSourceFactor(category)


        val liveDataPagelist = LivePagedListBuilder(sourceFactor, 10).build()

        return Listing<Bangumi>(
            liveDataPagelist = liveDataPagelist,
            netWordState = sourceFactor.sourceLiveData.switchMap { it.netWordState },
            retry = { sourceFactor.sourceLiveData.value?.retry() },
            initialLoad = sourceFactor.sourceLiveData.switchMap { it.initialLoadLiveData }
        )

    }


    /**
    [
    {"dayOfWeek": 1,
    "seasons": [
    {
    "id": "1720",
    "ji": "第12话",
    "name": "冰海战记",
    "pic": "https://img1.doubanio.com/view/photo/s_ratio_poster/public/p2558471577.jpg",
    "time": "2019-07-10"
    },
    .....]},

    {dayOfWeek": 2,
    "seasons": [
    {
    "id": "9",
    "ji": "第367话",
    "name": "银魂",
    "pic": "https://ws3.sinaimg.cn/large/005BYqpgly1g0wwiogt2tj307i0abt9d.jpg",
    "time": "2018-03-11"
    },
    ....]},
    ......
    ]
     */
    override suspend fun getBangumiTimeTable(): List<List<Bangumi>> = withContext(Dispatchers.IO) {
        val timeTableUrl = "$BASE_URL/type/week.php"
        val jsonObject = JSONObject(getResponseData(timeTableUrl))
        val timeTableData = jsonObject.takeIf { !it.isNull("result") }?.getJSONArray("result")
            ?: return@withContext emptyList<List<Bangumi>>()

        return@withContext ArrayList<List<Bangumi>>().apply {
            timeTableData.forEach { dayObject ->
                val childList = ArrayList<Bangumi>()
                val bangumis = dayObject.getJSONArray("seasons")

                bangumis.forEach { bangumiObject ->
                    //把ji的值放到remarks方便解析
                    bangumiObject.put("remarks", bangumiObject.getString("ji"))
                    childList.add(parserToBangumi(bangumiObject))
                }
                this.add(childList)
            }
        }
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
        val url = "$PC_URL/vod-detail-id-$id.html"
        val request = Request.Builder().run {
            addHeader("User-Agent", PHONE_REQUEST)
            url(url)
            build()
        }

        val document = Jsoup.parse(getResponseData(request))

        val niandai = document.getElementsByAttributeValue("itemprop", "uploadDate")
            .getOrNull(0)?.attr("content") ?: ""

        val cast =
            document.getElementsByAttributeValue("itemprop", "actor")
                .getOrNull(0)
                ?.run { this.attr("content").replace(",", "\n") }
                ?: ""

        val intro = document.getElementsByClass("leo-color-e leo-fs-s leo-ellipsis-2")
            .getOrNull(0)?.text() ?: ""

        val jiTotal =
            document.getElementsByClass("leo-color-a leo-fs-l leo-ellipsis-1")
                .getOrNull(0)
                ?.run { text().split("|")[1].trim() }
                ?: ""

        val staff =
            document.getElementsByClass("leo-ellipsis-1 leo-fs-s leo-lh-ss")
                .getOrNull(0)?.text() ?: ""

        val cover =
            document.getElementsByClass("leo-lazy leo-radius-s")
                .getOrNull(0)
                ?.run { attr("data-original") }
                ?: ""

        val name =
            document.getElementsByClass("leo-lazy leo-radius-s")
                .getOrNull(0)?.attrAlt() ?: ""

        BangumiDetail(
            id = id,
            source = BangumiSource.Zzzfun,
            name = name,
            cover = cover,
            niandai = niandai,
            cast = cast,
            staff = staff,
            jiTotal = jiTotal,
            intro = intro
        )
    }

    override suspend fun getJiList(id: String): Pair<Int, List<JiItem>> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/list.php?id=$id"
            val jsonObject = JSONObject(getResponseData(url))
            val jiList = ArrayList<JiItem>()
            var line = 0

            //线路1
            jsonObject.takeIf { !it.isNull("result") }?.let {
                val jiListWithVideoUrls = parserToJiList(it.getJSONArray("result"))
                jiList.addAll(jiListWithVideoUrls.jiList)
                linkVideoUrls.append(0, jiListWithVideoUrls.videoUrls)
                line++
            }

            //线路2
            jsonObject.takeIf { it.get("result2") !is String }?.let {
                val jiListWithVideoUrls = parserToJiList(it.getJSONArray("result2"))
                linkVideoUrls.append(1, jiListWithVideoUrls.videoUrls)
                line++
            }
            line to jiList
        }

    private fun parserToJiList(jsonArray: JSONArray): JiListAndVideoUrls {
        val list = ArrayList<JiItem>()
        val videoUrls = ArrayList<String>()

        jsonArray.forEach {
            list.add(JiItem(it.getString("ji")))
            videoUrls.add("$BASE_URL/play.php?url=${it.getString("id")}")
        }

        return JiListAndVideoUrls(list, videoUrls)
    }

    override suspend fun getPlayerUrl(id: String, ji: Int, line: Int): VideoUrl =
        withContext(Dispatchers.IO) {
            if (linkVideoUrls.isEmpty()) {
                VideoUrl("$PC_URL/index.php/vod-detail-id-$id.html", true)
            } else {
                VideoUrl(linkVideoUrls[line][ji])
            }
        }

    override suspend fun getDanmakuParser(id: String, ji: Int): BaseDanmakuParser? =
        withContext(Dispatchers.IO) {
            val url = "http://111.230.89.165:8089/zapi/dm.php?id%5B%5D=$id&id%5B%5D=${ji + 1}"
            val iLoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            iLoader.load(OkhttpUtil.getResponseBody(url).byteStream())
            ZzzFunDanmakuParser().apply { load(iLoader.dataSource) }
        }

    /**
    "id": "73",
    "pic": "https://ws4.sinaimg.cn/large/006AdpFDgy1fw337n10i3j30cs0jkq52.jpg",
    "name": "大剑",
    "hit": "24"
     */
    override suspend fun getRecommendBangumis(id: String): List<Bangumi> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/type/rnd.php"
            val jsonArray = JSONObject(getResponseData(url))
                .takeIf { !it.isNull("result") }
                ?.getJSONArray("result")
                ?: return@withContext emptyList<Bangumi>()

            val bangumis = ArrayList<Bangumi>()
            jsonArray.forEach {
                val bangumiId = it.getString("id")
                val cover = it.getString("pic")
                val name = it.getString("name")
                bangumis.add(Bangumi(bangumiId, BangumiSource.Zzzfun, name, cover))
            }
            bangumis
        }


}

private class ZzzfunSearchResultDataSource(
    private val searchWord: String
) : PageResultDataSourch<Int, Bangumi>(searchWord) {
    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>) {


        val url = "$SEARCH_URL${URLEncoder.encode(searchWord, "UTF-8")}"

        val jsonObject = JSONObject(getResponseData(url)).takeIf { !it.isNull("list") }

        if (jsonObject == null) {
            initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, true))
            return callback.onResult(emptyList(), null, null)
        }

        val bangumis = ArrayList<Bangumi>()
        jsonObject.getJSONArray("list").forEach {
            val name = it.getString("vod_name")
            val id = it.getString("vod_id")
            val cover = it.getString("vod_pic")

            val ji = when {
                it.getString("vod_remarks").isNotEmpty() -> it.getString("vod_remarks")
                it.getString("vod_serial").isNotEmpty() -> "更新至${it.getString("vod_serial")}集"
                else -> "全${it.getString("vod_total")}集"
            }

            bangumis.add(Bangumi(id, BangumiSource.Zzzfun, name, cover, ji))
        }
        callback.onResult(bangumis, null, null)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, bangumis.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

    }

}

private data class JiListAndVideoUrls(
    val jiList: List<JiItem>,
    val videoUrls: List<String>
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
) : PageResultDataSourch<Int, Bangumi>(category) {
    private val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType()
    private val bangumiDetailDao = AppDataBase.getInstance().bangumiDetailDao()


    override fun initialLoad(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>) {

        val page = 1
        val result = parserCategoryData(category, page)
        callback.onResult(result, null, page + 1)
        initialLoadLiveData.postValue(InitialLoad(NetWordState.SUCCESS, result.isEmpty()))
    }

    override fun afterload(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {
        val page = params.key
        val result: List<Bangumi> = try {
            parserCategoryData(category, page)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
        callback.onResult(result, if (result.isEmpty()) null else page + 1)
    }

    private fun parserCategoryData(category: String, page: Int): List<Bangumi> {
        var categoryName = category
        var url = "$BASE_URL/type/list.php"
        when (categoryName) {
            "最近更新" -> url = "$BASE_URL/type/hot2.php?page=$page"
            "日本动漫" -> categoryName = "1"
            "影视剧" -> categoryName = "4"
        }
        //POST请求
        val requestJaon = "{\"pageNow\":$page,\"tagNames\":\"$categoryName\"}"
        val requestBody = requestJaon.toRequestBody(jsonMediaType)
        val responseData = getResponseData(url, RequestMode.POST, requestBody).run {
            substring(indexOf("{"), lastIndexOf("}") + 1)
        }

        val jsonObject =
            JSONObject(responseData).takeIf { !it.isNull("result") } ?: return emptyList()

        return jsonObject.getJSONArray("result").run {
            val bangumis = ArrayList<Bangumi>()
            this.forEach {
                val name = it.getString("name")
                val cover = it.getString("pic")
                val id = it.getString("id")
                val isFollow = bangumiDetailDao.isFollowingBangumi(id, BangumiSource.Zzzfun.name)
                bangumis.add(CategoryBangumi(id, name, BangumiSource.Zzzfun, cover, isFollow = isFollow))
            }
            bangumis
        }
    }
}

private class CategoryPageDataSourceFactor(
    private val category: String
) : DataSource.Factory<Int, Bangumi>() {
    val sourceLiveData = MutableLiveData<CategoryPageDataSource>()

    override fun create(): DataSource<Int, Bangumi> = CategoryPageDataSource(category).apply {
        sourceLiveData.postValue(this)
    }
}