package com.tyxapp.bangumi_jetpack.main.history

import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import com.tyxapp.bangumi_jetpack.repository.HistoryWitchRepository

class HistoryViewModel(
    private val repository: HistoryWitchRepository
) : ViewModel() {

    val historyBangumis = LivePagedListBuilder(
        repository.getHistoryWitchBagnumis(),
        10
    ).build()


}
