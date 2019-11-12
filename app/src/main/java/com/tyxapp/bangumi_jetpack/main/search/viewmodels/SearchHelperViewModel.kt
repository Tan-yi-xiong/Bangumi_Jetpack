package com.tyxapp.bangumi_jetpack.main.search.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.SearchWord
import com.tyxapp.bangumi_jetpack.repository.SearchHelperRepository
import kotlinx.coroutines.*

class SearchHelperViewModel(
    private val repository: SearchHelperRepository
) : ViewModel() {
    val searchWords by lazy {
        MutableLiveData<List<SearchWord>>().apply {
            onTextChange("")
        }
    }
    val clickSearchWord = MutableLiveData<SearchWord>()

    private var getSearchWordsJob: Job? = null

    fun onSearchHelperItemClick(searchWord: SearchWord) {
        viewModelScope.launch {
            clickSearchWord.value = searchWord
            searchWord.time = System.currentTimeMillis()
            repository.insertSearchWord(searchWord)
        }
    }

    fun onSearchHelperItemLongClick(searchWord: SearchWord) {
        viewModelScope.launch {
            repository.removeSearchWord(searchWord)
            onTextChange("")//显示更新数据库后数据
        }
    }

    fun saveSearchWord(searchWord: String) {
        viewModelScope.launch {
            repository.insertSearchWord(
                SearchWord(
                    word = searchWord,
                    time = System.currentTimeMillis(),
                    isFromNet = false
                )
            )
        }
    }

    fun onTextChange(textKey: String) {
        //取消上一次请求, 以最新输入为准
        getSearchWordsJob?.cancel()
        getSearchWordsJob = null

        getSearchWordsJob = viewModelScope.launch {
            searchWords.value = repository.getSearchWords(textKey)
        }
    }
}
