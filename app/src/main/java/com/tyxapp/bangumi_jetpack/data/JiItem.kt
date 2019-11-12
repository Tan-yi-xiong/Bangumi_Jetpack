package com.tyxapp.bangumi_jetpack.data

/**
 * 播放页面选集的bean
 *
 */
data class JiItem(
    val text: String,//集名

    var isSelect: Boolean = false//是否为选中
)