package com.tyxapp.bangumi_jetpack.data

class DiliBangumi(
    id: String,
    source: BangumiSource,
    name: String,
    cover: String,
    isFollow: Boolean,
    val ji: String = "",
    val intro: String,
    val kandian: String,
    val type: String
) : CategoryBangumi(id, name, source, cover, isFollow)