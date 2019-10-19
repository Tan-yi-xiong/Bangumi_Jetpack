package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.*
import com.tyxapp.bangumi_jetpack.data.SearchResultChildRepository
import com.tyxapp.bangumi_jetpack.utilities.info
import kotlinx.coroutines.launch

class SearchResultChildViewModel(
    private val repository: SearchResultChildRepository
) : ViewModel() {
    private val searchWord = MutableLiveData<String>()
    private val searchResultListing = searchWord.map {
        repository.getSearchResult(it)
    }

    val searchResults = searchResultListing.switchMap { it.liveDataPagelist }

    val netWordState = searchResultListing.switchMap { it.netWordState }

    val initialLoad = searchResultListing.switchMap { it.initialLoad }

    fun setSearchWord(word: String) {
        if (searchWord.value != word) {
            searchWord.value = word
        }
    }

    fun retry(): () -> Unit = {
        viewModelScope.launch {
            searchResultListing.value?.retry?.invoke()
        }
    }

}
