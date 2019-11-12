package com.tyxapp.bangumi_jetpack.player

import androidx.lifecycle.*
import com.tyxapp.bangumi_jetpack.data.*
import com.tyxapp.bangumi_jetpack.repository.PlayerRepository
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import kotlinx.coroutines.*
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser

class PlayerViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val bangumiDetail = MutableLiveData<BangumiDetail>()
    private val lineAndjiItems = MutableLiveData<Pair<Int, List<JiItem>>>()

    val recommendBangumis = MutableLiveData<List<Bangumi>>() //推荐番剧

    val playerUrl = MutableLiveData<VideoUrl>() //视频播放地址

    val currentJiPositionLiveData = MutableLiveData<Int>() //当前集

    val jiItems: LiveData<List<JiItem>> = lineAndjiItems.map { it.second } //集数

    val playerUIState = MutableLiveData<UIStata>() //UI状态

    val isFollowLiveData = MutableLiveData<Boolean>() //是否为追番

    val clickBangumiLiveData = MutableLiveData<Bangumi>() //推荐番剧点击的番剧

    val videoRecordLiveData = MutableLiveData<VideoRecord>() //视频进度

    val baseDanmakuParserLiveData = MutableLiveData<BaseDanmakuParser>() // 弹幕

    val alearMessage = MutableLiveData<String>() // 提示消息

    var currentLine: Int = -1 //当前线路
        set(position) {
            if (position != field) {
                launch({
                    field = position
                    if (currentJiPositionLiveData.value == null) {
                        val lastWatchJi = bangumiDetail.value!!.lastWatchJi
                        val jiSize = lineAndjiItems.value!!.second.size
                        currentJiPositionLiveData.value =
                            if (lastWatchJi > jiSize - 1) 0 else lastWatchJi
                    } else {
                        currentJiPositionLiveData.value = currentJiPositionLiveData.value!!
                    }

                    // 更新最后观看时线路
                    bangumiDetail.value!!.lastWatchLine = position
                    playerRepository.updateBangumiDetail(bangumiDetail.value!!)
                })
            }
        }

    //线路集合
    val lines: LiveData<Array<String?>> = lineAndjiItems.map {
        val lineCount = it.first
        val linesNames = arrayOfNulls<String>(lineCount)
        for (i in 1..lineCount) {
            linesNames[i - 1] = "线路$i"
        }
        linesNames
    }

    @Suppress("UNCHECKED_CAST")
    fun loadData(id: String) {
        launch({

            playerUIState.value = UIStata.LOADING
            //并发请求
            val detail = asyncTry { playerRepository.getBangumiDetail(id) }
            val lineAndjiItemsData = asyncTry { playerRepository.getlineWithJiItem(id) }

            //番剧详情条目数据
            when (val returnData = detail.await()) {
                is Exception -> throw returnData
                else -> bangumiDetail.value = (returnData as BangumiDetail).apply {
                    this@PlayerViewModel.isFollowLiveData.value = this.isFollow
                }
            }

            playerUIState.value = UIStata.SUCCESS

            //选集条目数据
            when (val returnData = lineAndjiItemsData.await()) {
                is Exception -> throw returnData
                else -> {
                    returnData as Pair<Int, List<JiItem>>
                    val jiList = returnData.second
                    val lines = returnData.first
                    if (lines <= 0 || jiList.isEmpty()) {
                        playerUIState.value = UIStata.DATA_EMPTY
                    } else {
                        lineAndjiItems.value = returnData

                        //线路初始化
                        val lastWatchLine = bangumiDetail.value!!.lastWatchLine
                        currentLine = if (lastWatchLine > lines - 1) 0 else lastWatchLine

                        //成功后请求推荐番剧
                        recommendBangumis.value = playerRepository.getRecommendBangumis(id)
                    }

                }
            }
        })
    }

    /**
     * 追番按钮点击
     *
     */
    fun onFollowButtonClick(isSelect: Boolean) {
        launch({
            bangumiDetail.value!!.isFollow = isSelect
            playerRepository.updateBangumiDetail(bangumiDetail.value!!)
            isFollowLiveData.value = isSelect
        })
    }

    /**
     * 下载视频
     *
     */
    fun downLoadVideo() {
        launch({
            try {
                val filename = lineAndjiItems.value!!.second[currentJiPositionLiveData.value!!].text

                val message =
                    playerRepository.downLoadVideo(
                        bangumiDetail.value!!,
                        playerUrl.value!!.url,
                        filename
                    )
                message?.let { alearMessage.value = it }

                if (bangumiDetail.value?.isDownLoad == false) {
                    bangumiDetail.value!!.isDownLoad = true
                    playerRepository.updateBangumiDetail(bangumiDetail.value!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("下载出错!")
            }
        })

    }

    /**
     * 点击集数事件响应, 记录看到的集数
     *
     */
    fun onJiClick(position: Int) {
        if (currentJiPositionLiveData.value != position) {
            launch({
                currentJiPositionLiveData.value = position
                bangumiDetail.value!!.lastWatchJi = position
                playerRepository.updateBangumiDetail(bangumiDetail.value!!)
            })

        }
    }

    /**
     * 推荐番剧条目点击
     *
     */
    fun onRecommendBangumiClick(bangumi: Bangumi) {
        clickBangumiLiveData.value = bangumi
    }

    /**
     * 获取视频播放地址
     */
    fun getVideoUrl(id: String, ji: Int) {
        launch({

            try {
                //弹幕获取
                val danmakuParser = playerRepository.getDanmakuParser(id, ji)
                if (danmakuParser != null) {
                    baseDanmakuParserLiveData.value = danmakuParser
                }

                val videoUrl = playerRepository.getPlayerUrl(id, ji, currentLine)
                playerUrl.value = videoUrl

            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("视频播放地址加载出错")
            }
        })
    }

    /**
     * 获取最后一次观看的进度
     *
     */
    fun getVideoRecor(url: String) {
        launch({
            try {
                val videoRecord = playerRepository.getVideoRecord(url)
                if (videoRecord != null) {
                    videoRecordLiveData.value = videoRecord
                }
            } catch (e: Exception) {
                LOGI(e.toString())
                throw Exception("获取视频进度出错")
            }
        })

    }

    /**
     * 下一集
     *
     */
    fun nextJi() {
        currentJiPositionLiveData.value?.let {
            if (it + 1 >= lineAndjiItems.value!!.second.size) {
                alearMessage.value = "已经是最后一集了~"
            } else {
                alearMessage.value = "正在加载下一集..."
                currentJiPositionLiveData.value = it + 1
            }
        }
    }

    private fun errorHandle(throwable: Throwable) {
        throwable.printStackTrace()
        playerUIState.value = UIStata.error(throwable.message ?: "")
    }

    private inline fun launch(
        crossinline action: suspend CoroutineScope.() -> Unit,
        crossinline errorAction: (Throwable) -> Unit = ::errorHandle
    ) {

        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                errorAction(e)
            }
        }
    }

    /**
     * async 操作异常没有传到父协程, 请求操作发生错误应用直接闪退, 所以手动捕获异常, 上报父协程
     *
     */
    private inline fun <R> CoroutineScope.asyncTry(crossinline action: suspend () -> R) = async {
        try {
            action()
        } catch (e: Exception) {
            e
        }
    }
}