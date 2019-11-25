package com.tyxapp.bangumi_jetpack.data.parsers

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.InitialLoad
import com.tyxapp.bangumi_jetpack.data.NetWordState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

abstract class PageResultDataSourch<T, B>(
    private val searchWord: String?
) : PageKeyedDataSource<T, B>() {

    val encodeSearchWord: String by lazy(LazyThreadSafetyMode.NONE) {
        URLEncoder.encode(searchWord, "UTF-8")
    }
    val netWordState = MutableLiveData<NetWordState>()
    private var retry: (() -> Unit)? = null
    val initialLoadLiveData = MutableLiveData<InitialLoad>()

    suspend fun retry() {
        val prevRetry = retry
        retry = null//防止连续点击
        withContext(Dispatchers.IO) {
            prevRetry?.invoke()
        }
    }

    override fun loadInitial(
        params: LoadInitialParams<T>,
        callback: LoadInitialCallback<T, B>
    ) {
        try {
            initialLoadLiveData.postValue(InitialLoad(NetWordState.LOADING))
            retry = null
            initialLoad(params, callback)
        } catch (e: Exception) {
            e.printStackTrace()
            retry = {
                loadInitial(params, callback)
            }
            val initialLoad = InitialLoad(NetWordState.error(e.toString()))
            initialLoadLiveData.postValue(initialLoad)
        }
    }

    abstract fun initialLoad(params: LoadInitialParams<T>, callback: LoadInitialCallback<T, B>)

    override fun loadAfter(params: LoadParams<T>, callback: LoadCallback<T, B>) {
        try {
            netWordState.postValue(NetWordState.LOADING)
            retry = null
            afterload(params, callback)
            netWordState.postValue(NetWordState.SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            retry = {
                loadAfter(params, callback)
            }
            netWordState.postValue(NetWordState.error(e.toString()))
        }
    }

    abstract fun afterload(params: LoadParams<T>, callback: LoadCallback<T, B>)

    override fun loadBefore(params: LoadParams<T>, callback: LoadCallback<T, B>) {

    }

}