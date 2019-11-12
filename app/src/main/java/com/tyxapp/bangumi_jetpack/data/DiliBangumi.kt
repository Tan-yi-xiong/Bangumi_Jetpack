package com.tyxapp.bangumi_jetpack.data

class DiliBangumi(
    id: String,
    source: BangumiSource,
    name: String,
    cover: String,
    ji: String = "",
    val intro: String,
    val kandian: String,
    val type: String
) : Bangumi(id, source, name, cover, ji)