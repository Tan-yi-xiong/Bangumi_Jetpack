package com.tyxapp.bangumi_jetpack.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.repository.HomeDataRepository
import com.tyxapp.bangumi_jetpack.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MainRepository
) : ViewModel() {
    // 主页的FragmentViewmodel共享的Repository
    val homeDataRepository = MutableLiveData<HomeDataRepository>()
    val shouldShowUpdateDialog = MutableLiveData<Boolean>()
    val alearMessage = MutableLiveData<String>()

    fun checkAppUpdate() {
        viewModelScope.launch {
            try {
                val packageInfo = BangumiApp.getContext()
                    .packageManager
                    .getPackageInfo(BangumiApp.getContext().packageName, 0)
                val currVersionCode = packageInfo.versionCode
                val currVersionName = packageInfo.packageName

                val (newVersionCode: Int, newVersionName: String) = repository.getAppVersionFromNet()

                if (newVersionCode != currVersionCode || newVersionName != currVersionName) {
                    shouldShowUpdateDialog.value = true
                } else {
                    alearMessage.value = "已是最新版本"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}