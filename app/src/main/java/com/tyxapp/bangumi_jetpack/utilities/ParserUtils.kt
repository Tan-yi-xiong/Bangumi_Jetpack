package com.tyxapp.bangumi_jetpack.utilities

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    for (i in 0 until length()) {
        action(getJSONObject(i))
    }
}

fun JSONObject.replace(name: String, value: Any) {
    remove(name)
    put(name, value)
}

fun getHtmlBangumiId(str: String): String? = Regex("""\d+""").find(str)?.value

fun Element.get_a_tags(): Elements = getElementsByTag("a")

fun Element.attrHref(): String = attr("href")

fun Element.get_img_tags(): Elements = getElementsByTag("img")

fun Element.attrAlt(): String = attr("alt")

fun Element.get_p_tags(): Elements = getElementsByTag("p")

fun Element.attrSrc(): String = attr("src")

