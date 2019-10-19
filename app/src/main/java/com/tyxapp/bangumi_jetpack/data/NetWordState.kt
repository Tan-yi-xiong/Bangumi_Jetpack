package com.tyxapp.bangumi_jetpack.data

enum class State {
    LOADING, ERROR, SUCCESS
}

class NetWordState(
    val state: State,
    val msg: String? = null
) {
    companion object {
        val SUCCESS = NetWordState(State.SUCCESS)
        val LOADING = NetWordState(State.LOADING)
        fun error(msg: String?) = NetWordState(State.ERROR, msg)
    }
}