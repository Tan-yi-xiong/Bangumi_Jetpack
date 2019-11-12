package com.tyxapp.bangumi_jetpack.utilities

import android.text.format.Formatter
import com.tyxapp.bangumi_jetpack.BangumiApp
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

const val PHONE_REQUEST =
    "Mozilla/5.0 (Linux; Android 4.4.4; HTC D820u Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.89 Mobile Safari/537.36"

inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    for (i in 0 until length()) {
        action(getJSONObject(i))
    }
}

fun JSONObject.replace(name: String, value: Any) {
    remove(name)
    put(name, value)
}

fun getHtmlBangumiId(str: String): String? = str.substring(str.lastIndexOf("/") + 1, str.lastIndexOf("."))

fun Element.get_a_tags(): Elements = getElementsByTag("a")

fun Element.attrHref(): String = attr("href")

fun Element.get_img_tags(): Elements = getElementsByTag("img")

fun Element.attrAlt(): String = attr("alt")

fun Element.get_p_tags(): Elements = getElementsByTag("p")

fun Element.attrSrc(): String = attr("src")

fun formatFileSize(byteSize: Long) = Formatter.formatFileSize(BangumiApp.getContext(), byteSize)

fun unicodeToString(unicode: String): String {
    return buildString {
        var i = -1
        val charArray = unicode.toCharArray()
        charArray.forEachIndexed { index, c ->
            if (index <= i) return@forEachIndexed
            if (c == '\\') {
                val unicodeString = buildString {
                    for (j in 2..5) {
                        this.append(charArray[index + j])
                    }
                }
                append(unicodeString.toInt(16).toChar())
                i = index + 5
            } else {
                append(c)
            }
        }
    }.also {
        print(it)
    }
}