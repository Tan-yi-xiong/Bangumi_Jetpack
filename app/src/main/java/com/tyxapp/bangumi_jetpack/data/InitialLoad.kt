package com.tyxapp.bangumi_jetpack.data

data class InitialLoad(
    val netWordState: NetWordState,
    val isDataEmpty: Boolean = false
)