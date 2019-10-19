package com.tyxapp.bangumi_jetpack.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class Listing<T>(
    val liveDataPagelist: LiveData<PagedList<T>>,
    val netWordState: LiveData<NetWordState>,
    val retry: (suspend () -> Unit)?,
    val initialLoad: LiveData<InitialLoad>
)