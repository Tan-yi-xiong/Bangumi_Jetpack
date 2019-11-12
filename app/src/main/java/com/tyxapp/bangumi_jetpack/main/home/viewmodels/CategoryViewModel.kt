package com.tyxapp.bangumi_jetpack.main.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.data.CategorItem
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.launch
import java.lang.Exception

class CategoryViewModel(private var mRepository: HomeDataRepository) : ViewModel() {
    val categorys: MutableLiveData<List<CategorItem>> by lazy {
        MutableLiveData<List<CategorItem>>().apply {
            getCategorys()
        }
    }

    val error = MutableLiveData<Throwable>()

    val empty = MutableLiveData<Boolean>()

    val loading = MutableLiveData<Boolean>()


    private fun getCategorys() {
        viewModelScope.launch {
            loading.value = true
            try {
                val data = mRepository.getCategorItems()
                if (data.isEmpty()) {
                    empty.value = true
                } else {
                    categorys.value = data
                }
                loading.value = false
            } catch (e:Exception) {
                errorHandle(e)
            }
        }
    }

    fun refresh() = getCategorys()

    fun replaceViewModel(repository: HomeDataRepository) {
        mRepository = repository
        getCategorys()
    }

    private fun errorHandle(e: Exception) {
        error.value = e
        e.printStackTrace()
        if (loading.value == true) loading.value = false
    }
}
