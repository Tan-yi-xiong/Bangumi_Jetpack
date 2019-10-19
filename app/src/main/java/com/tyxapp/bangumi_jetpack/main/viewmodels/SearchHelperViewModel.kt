package com.tyxapp.bangumi_jetpack.main.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.SearchWord
import com.tyxapp.bangumi_jetpack.data.SearchHelperRepository
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

    private var getNetSearchWordsJob: Job? = null

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
        viewModelScope.launch {
            if (textKey.isEmpty()) {//空字符为请求数据库结果
                getNetSearchWordsJob?.cancel()//取消网络请求, 网络延迟会覆盖数据库结果
                getNetSearchWordsJob = null
                searchWords.value = repository.getSearchWords(textKey)
            } else {
                getNetSearchWordsJob?.cancel()
                getNetSearchWordsJob = CoroutineScope(this.coroutineContext).launch {
                    delay(50)//防止连续输入频繁网络请求
                    searchWords.value = repository.getSearchWords(textKey)
                    getNetSearchWordsJob = null
                }
            }
        }
    }
}
