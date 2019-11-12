package com.tyxapp.bangumi_jetpack.data

data class DownloadProgress(
    var id: Int,
    var progress: Long,
    var total: Long
)