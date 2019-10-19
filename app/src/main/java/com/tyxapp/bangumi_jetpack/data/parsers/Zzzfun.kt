package com.tyxapp.bangumi_jetpack.data.parsers

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.main.adapter.BANNER
import com.tyxapp.bangumi_jetpack.utilities.*
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil.getResponseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList


class Zzzfun : IHomePageParser, IsearchParse {

    private val baseUrl = "http://111.230.89.165:8089/zapi"
    private val categorItemName by lazy(LazyThreadSafetyMode.NONE) {
        BangumiApp.getContext()
            .resources.getStringArray(com.tyxapp.bangumi_jetpack.R.array.zzzfun_categor_name)
    }
    private val categorItemImages: IntArray by lazy(LazyThreadSafetyMode.NONE) {
        intArrayOf(
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_movie,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_dianshiju,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_zhenren,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_season_spring,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_season_summer,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_season_autumn,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_season_winter,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_guocan,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_teleplay,
            com.tyxapp.bangumi_jetpack.R.drawable.zzzfun_category_japan_bangumi
        )
    }

    /**
     * zzzfun主页分为6部分, URL为 /type/home.php?t=1
     * t的值分别为9(头部轮播), 42, 1-4,
     */
    override fun getHomeBangumis(): Map<String, List<Bangumi>> {
        val titles = BangumiApp.getContext()
            .resources.getStringArray(com.tyxapp.bangumi_jetpack.R.array.zzfun_title)
        val map = LinkedHashMap<String, List<Bangumi>>()
        for (i in 0 until 6) {
            val title = titles[i]
            var position = i
            if (i == 0) {
                position = 42
            } else if (i == 5) {
                position = 9
            }
            val url = "$baseUrl/type/home.php?t=$position"
            val jsonObject = JSONObject(getResponseData(url))
            val bangumis = jsonObject.run {
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
            map[title] = bangumis
        }
        return map
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

    override fun getCategorItems(): List<CategorItem> {
        val list = ArrayList<CategorItem>()
        categorItemImages.forEachIndexed { index, i ->
            val categorItem = CategorItem(i, categorItemName[index])
            list.add(categorItem)
        }
        return list
    }

    override fun getCategoryBangumis(category: String): Listing<Bangumi> {
        val sourceFactor = CategoryPageDataSourceFactor(category)


        val liveDataPagelist = LivePagedListBuilder(sourceFactor, 10).build()

        return Listing<Bangumi>(
            liveDataPagelist = liveDataPagelist,
            netWordState = Transformations.switchMap(sourceFactor.sourceLiveData) {
                it.netWordState
            },
            retry = { sourceFactor.sourceLiveData.value?.retry() }
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
    override fun getBangumiTimeTable(): List<List<Bangumi>> {
        val timeTableUrl = "$baseUrl/type/week.php"
        val jsonObject = JSONObject(getResponseData(timeTableUrl))
        val timeTableData = jsonObject.takeIf { !it.isNull("result") }?.getJSONArray("result")
            ?: return emptyList()

        val list = ArrayList<List<Bangumi>>()
        timeTableData.forEach { dayObject ->
            val childList = ArrayList<Bangumi>()
            val bangumis = dayObject.getJSONArray("seasons")

            bangumis.forEach { bangumiObject ->
                //把ji的值放到remarks方便解析
                bangumiObject.put("remarks", bangumiObject.getString("ji"))
                childList.add(parserToBangumi(bangumiObject))
            }
            list.add(childList)
        }
        return list
    }

    override fun getSearchResult(searchWord: String): Listing<Bangumi> {
        val factor = SearchResultDataSourceFactor(searchWord)

        val livePagedList = LivePagedListBuilder(factor, 10).build()

        return Listing(
            liveDataPagelist = livePagedList,
            netWordState = factor.searchResultDataSource.switchMap { it.netWordState },
            retry = { factor.searchResultDataSource.value?.retry() }
        )
    }

}

private class SearchResultDataSource(
    private val searchWord: String
) : PageKeyedDataSource<Int, Bangumi>() {

    val netWordState = MutableLiveData<NetWordState>()
    private var retry: (() -> Unit)? = null

    suspend fun retry() {
        val prveRetry = retry
        retry = null
        withContext(Dispatchers.IO) {
            prveRetry?.invoke()
        }
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>
    ) {
        try {
            netWordState.postValue(NetWordState.LOADING)
            val url =
                "http://111.230.89.165:8099/api.php/provvde/vod/?ac=list&wd=${URLEncoder.encode(
                    searchWord,
                    "UTF-8"
                )}"

            val jsonObject = JSONObject(getResponseData(url)).takeIf { !it.isNull("list") }
                ?: return callback.onResult(emptyList(), null, null)

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
            netWordState.postValue(NetWordState.SUCCESS)
        } catch (e: Exception) {
            retry = {
                loadInitial(params, callback)
            }
            netWordState.postValue(NetWordState.error(e.toString()))
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

    }

}

private class SearchResultDataSourceFactor(
    private val searchWord: String
) : DataSource.Factory<Int, Bangumi>() {
    val searchResultDataSource = MutableLiveData<SearchResultDataSource>()

    override fun create(): DataSource<Int, Bangumi> {
        return SearchResultDataSource(searchWord).apply {
            searchResultDataSource.postValue(this)
        }
    }

}

private class CategoryPageDataSource(
    private val category: String
) : PageKeyedDataSource<Int, Bangumi>() {
    private val jsonMediaType: MediaType = "application/json; charset=utf-8".toMediaType()
    val netWordState = MutableLiveData<NetWordState>()
    private var retry: (() -> Unit)? = null

    suspend fun retry() {
        val prevRetry = retry
        retry = null//防止连续点击
        withContext(Dispatchers.IO) {
            prevRetry?.invoke()
        }
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Bangumi>
    ) {
        val page = 1
        try {
            netWordState.postValue(NetWordState.LOADING)
            val result = parserCategoryData(category, page)
            retry = null
            netWordState.postValue(NetWordState.SUCCESS)
            callback.onResult(result, null, page + 1)
        } catch (e: Exception) {
            retry = {
                loadInitial(params, callback)
            }
            netWordState.postValue(NetWordState.error(e.toString()))
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {
        val page = params.key

        val result: List<Bangumi> = try {
            parserCategoryData(category, page)
        } catch (e: Exception) {
            info(e.toString())
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(BangumiApp.getContext(), "发生错误", Toast.LENGTH_SHORT).show()
            }
            emptyList()
        }
        callback.onResult(result, if (result.isEmpty()) null else page + 1)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Bangumi>) {

    }

    private fun parserCategoryData(category: String, page: Int): List<Bangumi> {
        var categoryName = category
        var url = "http://111.230.89.165:8089/zapi/type/list.php"
        when (categoryName) {
            "最近更新" -> url = "http://111.230.89.165:8089/zapi/type/hot2.php?page=$page"
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
                bangumis.add(Bangumi(id, BangumiSource.Zzzfun, name, cover))
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