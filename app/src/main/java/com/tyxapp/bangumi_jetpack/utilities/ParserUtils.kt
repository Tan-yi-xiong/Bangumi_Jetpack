package com.tyxapp.bangumi_jetpack.utilities

import org.json.JSONArray
import org.json.JSONObject

inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    for (i in 0 until length()) {
        action(getJSONObject(i))
    }
}

fun JSONObject.replace(name: String, value: Any) {
    remove(name)
    put(name, value)
}

