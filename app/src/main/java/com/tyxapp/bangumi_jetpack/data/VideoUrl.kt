package com.tyxapp.bangumi_jetpack.data

/**
 * 视频播放地址bean
 *
 */
data class VideoUrl(
    val url: String,//播放地址

    val isJumpToBrowser: Boolean = false//是否为跳转到浏览器url
)