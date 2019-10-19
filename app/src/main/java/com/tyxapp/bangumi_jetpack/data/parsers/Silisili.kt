package com.tyxapp.bangumi_jetpack.data.parsers

import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.utilities.*
import org.jsoup.Jsoup

class Silisili : IHomePageParser {
    private val BASEURL_PC = "http://silisili.me"
    private val BASEURL_PHONE = "http://m.silisili.me"

    override fun getHomeBangumis(): Map<String, List<Bangumi>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCategorItems(): List<CategorItem> {
        val url = "$BASEURL_PHONE/dm"
        val document = Jsoup.parse(OkhttpUtil.getResponseData(url))
        val categorItems = ArrayList<CategorItem>()

        document.getElementsByClass("plist01").forEach { categorList ->
            categorList.children().forEach {
                val categorName = it.get_p_tags()[0].text()
                val cover = BASEURL_PHONE + it.get_img_tags()[0].attrSrc()
                categorItems.add(CategorItem(cover, categorName))
            }
        }
        return categorItems
    }

    override fun getCategoryBangumis(category: String): Listing<Bangumi> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBangumiTimeTable(): List<List<Bangumi>> {
        val document = Jsoup.parse(OkhttpUtil.getResponseData(BASEURL_PC))
        val weekBangumis = ArrayList<List<Bangumi>>()

        document.getElementsByClass("time_con").forEach {
            it.getElementsByClass("clear").forEach { dayElement ->
                val daybangumis = ArrayList<Bangumi>()
                dayElement.children().forEach { bangumiElement ->
                    val id = getHtmlBangumiId(bangumiElement.get_a_tags()[0].attrHref())
                        ?: throw NullPointerException("sili 时间表 解析id为空")

                    val imgTag = bangumiElement.get_img_tags()[0]
                    val name = imgTag.attrAlt()
                    val cover = imgTag.attrHref()

                    val ji = bangumiElement.getElementsByTag("i")[0].text()

                    daybangumis.add(Bangumi(id, BangumiSource.SiliSili, name, cover, ji))
                }
                weekBangumis.add(daybangumis)
            }
        }
        return weekBangumis
    }

}