package com.tyxapp.bangumi_jetpack.data.parsers

import androidx.paging.DataSource
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.CategorItem
import com.tyxapp.bangumi_jetpack.data.IHomePageParser
import com.tyxapp.bangumi_jetpack.main.home.adapter.BANNER
import com.tyxapp.bangumi_jetpack.utilities.OkhttpUtil.getResponseData
import com.tyxapp.bangumi_jetpack.utilities.info
import org.json.JSONArray
import org.json.JSONObject

class Zzzfun : IHomePageParser {
    private val baseUrl = "http://111.230.89.165:8089/zapi"
    private val categorItemName by lazy(LazyThreadSafetyMode.NONE) {
        BangumiApp.getContext().resources.getStringArray(R.array.zzzfun_categor_name)
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

    /**
     * zzzfun主页分为6部分, URL为 /type/home.php?t=1
     * t的值分别为9(头部轮播), 42, 1-4,
     */
    override fun getHomeBangumis(): Map<String, List<Bangumi>> {
        val titles = BangumiApp.getContext().resources.getStringArray(R.array.zzfun_title)
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

    override fun getCategoryBangumis(category: String): DataSource.Factory<Int, Bangumi> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun JSONArray.forEach(action: (JSONObject) -> Unit) {
        for (i in 0 until length()) {
            action(getJSONObject(i))
        }
    }

    private fun JSONObject.replace(name: String, value: Any) {
        remove(name)
        put(name, value)
    }

}