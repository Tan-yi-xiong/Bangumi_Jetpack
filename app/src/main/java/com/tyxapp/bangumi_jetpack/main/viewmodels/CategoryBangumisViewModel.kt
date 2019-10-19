package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.HomeDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class CategoryBangumisViewModel(
    private val repository: HomeDataRepository
) : ViewModel() {
    private val categoryWord = MutableLiveData<String>()

    private val listing = map(categoryWord) {
        repository.getCategoryBangumis(it)
    }

    val netWordState = switchMap(listing) { it.netWordState }

    val categoryBangumis = switchMap(listing) { it.liveDataPagelist }

    fun getCategoryBangumis(categoryWord: String) {
        if (this.categoryWord.value != categoryWord) {
            this.categoryWord.value = categoryWord
        }
    }

    fun retry() = viewModelScope.launch {
        listing.value?.retry?.invoke()
    }

}
