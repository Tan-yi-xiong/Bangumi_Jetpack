package com.tyxapp.bangumi_jetpack.data

/**
 * UI状态类, viewmodel通过此类控制UI
 *
 */
class UIStata(
    val netWordState: NetWordState,
    val isRefreshing: Boolean = false,
    val dataEmpty: Boolean = false
) {
    companion object{
        val LOADING = UIStata(NetWordState.LOADING)
        val SUCCESS = UIStata(NetWordState.SUCCESS)
        fun error(msg: String) = UIStata(NetWordState.error(msg))
        fun refreshing(isRefreshing: Boolean) = UIStata(
            if (isRefreshing) NetWordState.LOADING else NetWordState.SUCCESS,
            isRefreshing)
        val DATA_EMPTY = UIStata(NetWordState.SUCCESS, dataEmpty = true)
    }
}