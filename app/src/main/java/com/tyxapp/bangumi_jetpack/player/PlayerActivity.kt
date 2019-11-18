package com.tyxapp.bangumi_jetpack.player

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler
import com.kk.taurus.playerbase.entity.DataSource
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.receiver.ReceiverGroup
import com.kk.taurus.playerbase.widget.BaseVideoView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.JiItem
import com.tyxapp.bangumi_jetpack.data.parsers.ParserFactory
import com.tyxapp.bangumi_jetpack.databinding.ActivityPlayerBinding
import com.tyxapp.bangumi_jetpack.databinding.BangumiDetailItemBinding
import com.tyxapp.bangumi_jetpack.databinding.PlayerJiSelectItemBinding
import com.tyxapp.bangumi_jetpack.databinding.RecommendItemLayoutBinding
import com.tyxapp.bangumi_jetpack.player.adapter.*
import com.tyxapp.bangumi_jetpack.player.cover.*
import com.tyxapp.bangumi_jetpack.utilities.*
import com.tyxapp.bangumi_jetpack.views.ParallaxVideoView
import com.tyxapp.bangumi_jetpack.views.alert
import com.tyxapp.bangumi_jetpack.views.noButton
import com.tyxapp.bangumi_jetpack.views.yesButton
import org.jetbrains.anko.browse
import org.jetbrains.anko.toast
import kotlin.math.max

const val ARG_ID = "ARG_ID"
const val ARG_BANGUMI_SOURCE = "ARG_BANGUMI_SOURCE"

class PlayerActivity : BasePlayerActivity() {

    //binds
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var bangumiDetailItemBinding: BangumiDetailItemBinding
    private lateinit var jiSelectItemBinding: PlayerJiSelectItemBinding
    private lateinit var recommendBinding: RecommendItemLayoutBinding

    //views
    private lateinit var mRecyclerView: RecyclerView
    private val playerAdapter: PlayerAdapter = PlayerAdapter { viewModel.loadData(bangumiId) }
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var mScrimView: View
    private lateinit var mToolBar: Toolbar

    //datas
    private var scrimViewOffset: Int = 0

    private var videoViewHeight = 0
    private val bangumiId: String by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(ARG_ID)
            ?: throw NullPointerException("PlayerActivity应传入对应参数")
    }
    private val bangumiSource: BangumiSource by lazy(LazyThreadSafetyMode.NONE) {
        val sourceName = intent.getStringExtra(ARG_BANGUMI_SOURCE)
            ?: throw NullPointerException("PlayerActivity应传入对应参数")
        BangumiSource.valueOf(sourceName)
    }

    private var mSensorManager: SensorManager? = null
    private var isLandscape = false
    private val viewModel: PlayerViewModel by viewModels {
        InjectorUtils.providePlayerViewModelFactory(
            ParserFactory.createPlayerVideoParser(bangumiSource)
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        mSensorManager = getSystemService()
        init()
        addObserver()
    }

    private fun init() {
        initView()
        initBinding()
    }

    private fun initView() {
        mRecyclerView = binding.recyclerView
        setVideoView(binding.videoView)
        mScrimView = binding.scrimView
        mToolBar = binding.toolBar

        //ScrimView
        (mVideoView as ParallaxVideoView).setOnOffsetListener { offsetScrimView(it) }

        //toolBar
        mToolBar.setNavigationOnClickListener { finish() }
        mToolBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.player_setting) {
                val controlCover =
                    mReceiver.getReceiver<ControlCover>(ControlCover::class.java.simpleName)

                controlCover.showSettingBottomSheet()
                true
            } else {
                false
            }
        }

        //RecyclerView
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = playerAdapter
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val enableOffset =
                    (mVideoView.isInPlaybackState && !mVideoView.isPlaying) || (mVideoView as ParallaxVideoView).offset != 0

                if (enableOffset) {
                    val offset = bangumiDetailItemBinding.root.top
                    (mVideoView as ParallaxVideoView).offset = offset
                }
            }
        })

        //BaseVideoView
        setReceiverGroup(ReceiverGroup().apply {
            addReceiver(
                DanmuCover::class.java.simpleName,
                DanmuCover(this@PlayerActivity, viewModel)
            )
            addReceiver(
                ControlCover::class.java.simpleName,
                ControlCover(this@PlayerActivity, viewModel)
            )
            addReceiver(LoadingCover::class.java.simpleName, LoadingCover(this@PlayerActivity))
            addReceiver(GestureCover::class.java.simpleName, GestureCover(this@PlayerActivity))
            addReceiver(ErrorCover::class.java.simpleName, ErrorCover(this@PlayerActivity))
        })
        with(mVideoView) {
            setReceiverGroup(mReceiver)
            setEventHandler(onVideoViewEventHandler)
            post {
                videoViewHeight = mVideoView.height
                // activity销毁后恢复检查是否是横屏状态
                val isLandscape =
                    resources.configuration.orientation == Configuration.ORIENTATION_UNDEFINED
                if (isLandscape) setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }
        }

        //ImitationStateBar
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        var stateBarHeight = resources.getDimension(resId)
        stateBarHeight = if (stateBarHeight == 0f) {
            10.toPx().toFloat()
        } else {
            stateBarHeight
        }

        binding.stateBar.layoutParams.height = stateBarHeight.toInt()
    }

    private fun offsetScrimView(offset: Int) {
        if (scrimViewOffset == 0) {
            scrimViewOffset = binding.barLayout.height - mScrimView.height
        }
        val offsetin = max(offset, scrimViewOffset)
        if (mScrimView.translationY != offsetin.toFloat()) {
            mScrimView.translationY = offsetin.toFloat()
            if (offset <= -mVideoView.minimumHeight) {
                if (!mScrimView.isVisible) {
                    mScrimView.fadeIn()
                    val title = viewModel.bangumiDetail.value?.name ?: ""
                    mToolBar.title = title
                }
            } else {
                if (mScrimView.isVisible && mScrimView.tag != true) {
                    mScrimView.fadeOut(
                        startAction = {
                            mScrimView.tag = true //添加标记表示正在执行动画, 防止多次调用
                        },
                        endAction = {
                            mScrimView.tag = false
                            mScrimView.isGone(true)
                            mToolBar.title = ""
                        }
                    )
                }
            }
        }

    }

    private fun initBinding() {
        bangumiDetailItemBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.bangumi_detail_item,
            mRecyclerView, false
        )
        bangumiDetailItemBinding.viewModel = viewModel

        jiSelectItemBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.player_ji_select_item,
            mRecyclerView, false
        )
        recommendBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.recommend_item_layout,
            null, false
        )

        val behaviorView = binding.root.findViewById<View>(R.id.detail_bottom_sheeet)
        behavior = BottomSheetBehavior.from(behaviorView)

        binding.root.findViewById<View>(R.id.close).setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private val onVideoViewEventHandler = object : OnVideoViewEventHandler() {

        override fun requestPause(videoView: BaseVideoView?, bundle: Bundle?) {
            super.requestPause(videoView, bundle)
            isUserPause = true
        }

        override fun requestResume(videoView: BaseVideoView?, bundle: Bundle?) {
            super.requestResume(videoView, bundle)
            isUserPause = false
        }

        override fun onAssistHandle(assist: BaseVideoView?, eventCode: Int, bundle: Bundle?) {
            super.onAssistHandle(assist, eventCode, bundle)
            when (eventCode) {
                FULL_SCREEN_CODE -> { //横屏/竖屏
                    val isLandscape = bundle!!.getBoolean(FULL_SCREEN_KEY)
                    val orientation = if (isLandscape) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                    setOrientation(orientation)
                }

                REFRESH_VIDEO_CODE -> mVideoView.rePlay(mVideoView.currentPosition) //刷新视频

                SPEED_CONTROL_CODE -> {//视频变速
                    val speed = bundle!!.getFloat(SPEED_CONTROL_KEY)
                    mVideoView.setSpeed(speed)
                    mReceiver.groupValue.putFloat(SPEED_CONTROL_KEY, speed)
                }

                STATE_BAR_CODE -> {// 状态栏显示/隐藏
                    val isShow = bundle!!.getBoolean(STATE_BAR_KEY)
                    if (isShow) showStateBar() else hideStateBar()

                }

                ERROR_COVER_VISIBLE_CODE -> { // 错误页面是否可见
                    val isVisible = bundle!!.getBoolean(ERROR_COVER_VISIBLE_KEY)
                    if (isVisible) { // 可见屏蔽触摸事件
                        mVideoView.superContainer.setGestureEnable(false)
                    } else {
                        mVideoView.superContainer.setGestureEnable(true)
                    }
                }
            }
        }
    }

    private fun addObserver() = with(viewModel) {
        //番剧详情条目
        bangumiDetail.observe(this@PlayerActivity) {
            bindBangimiDetail(it)
        }

        //选集列表
        jiItems.observe(this@PlayerActivity) {
            bindJiItems(it)
        }
        //线路
        lines.observe(this@PlayerActivity) { lineNames ->
            bindSpainner(lineNames)
        }
        currentJiPositionLiveData.observe(this@PlayerActivity) {
            val adapter = jiSelectItemBinding.recyclerView.adapter as JiSelectAdapter
            val spinner = jiSelectItemBinding.spinner
            if (spinner.selectedItemPosition != currentLine) {
                spinner.setSelection(currentLine)
            }

            if (adapter.getCurrentJiName().isEmpty()) { // 滚动到最后观看集数
                jiSelectItemBinding.recyclerView.smoothScrollToPosition(it)
            }
            adapter.selectJi(it)
            getVideoUrl(bangumiId, it)
        }

        //视频播放订阅
        playerUrl.observe(this@PlayerActivity) { videoUrl ->
            val dataSource = DataSource(videoUrl.url)
            val jiAdapter = jiSelectItemBinding.recyclerView.adapter as JiSelectAdapter
            dataSource.title = "${bangumiDetail.value!!.name} ${jiAdapter.getCurrentJiName()}"
            mVideoView.setDataSource(dataSource)
            mVideoView.start()
            if (videoUrl.isJumpToBrowser) {
                alert(R.string.text_tips, R.string.text_video_parser_error) {
                    yesButton { browse(videoUrl.url) }
                    noButton { it.dismiss() }
                }.show()
            }
        }

        //推荐番剧数据订阅
        recommendBangumis.observe(this@PlayerActivity) {
            bindRecommend(it)
        }

        //ui状态
        playerUIState.observe(this@PlayerActivity) {
            playerAdapter.submitUIState(it)
        }

        //点击推荐番剧
        clickBangumiLiveData.observe(this@PlayerActivity) {
            startPlayerActivity(it.id, it.source.name)
            if (mVideoView.isInPlaybackState) {
                stopPlayer()
            }
        }

        //提示消息
        alearMessage.observe(this@PlayerActivity) { toast(it) }


        //加载数据
        viewModel.loadData(bangumiId)
    }


    private fun bindRecommend(recommends: List<Bangumi>?) {
        var adapter = recommendBinding.recommendRecyclerView.adapter as? RecommendAdapter
        if (adapter == null) {
            adapter = RecommendAdapter(viewModel)
            recommendBinding.recommendRecyclerView.adapter = adapter
        }
        adapter.submitList(recommends)
        if (!playerAdapter.hadAdd(RECOMMEND)) {
            playerAdapter.addRecommendView(recommendBinding.root)
        }
    }

    private fun bindJiItems(jiItems: List<JiItem>) {
        val recyclerView = jiSelectItemBinding.recyclerView
        if (recyclerView.adapter == null) {
            recyclerView.adapter = JiSelectAdapter(viewModel)
        }
        (recyclerView.adapter as JiSelectAdapter).submitList(jiItems)
        if (!playerAdapter.hadAdd(JI_SELECT)) {
            playerAdapter.addJiSelectView(jiSelectItemBinding.root)
        }

        jiSelectItemBinding.expansText.setOnClickListener {
            jiSelectItemBinding.expansionBotton.callOnClick()
        }
        jiSelectItemBinding.expansionBotton.setOnClickListener { view ->
            val angle: Float
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is GridLayoutManager) {
                angle = 0f
                recyclerView.layoutManager =
                    LinearLayoutManager(this@PlayerActivity, RecyclerView.HORIZONTAL, false)

                //滚动到最后选集的位置
                val adapter = recyclerView.adapter as JiSelectAdapter
                val position = adapter.findSelectItem()
                recyclerView.scrollToPosition(position)
            } else {
                angle = 90.0f
                TransitionManager.beginDelayedTransition(recyclerView, Slide(Gravity.BOTTOM))
                recyclerView.layoutManager = GridLayoutManager(this@PlayerActivity, 4)
            }
            view.animate().rotation(angle).setDuration(150).start()
        }
    }

    private fun bindBangimiDetail(bangumiDetail: BangumiDetail?) {
        binding.bangumiDetali = bangumiDetail

        bangumiDetailItemBinding.apply {
            this.bangumiDetail = bangumiDetail
            viewModel = viewModel
            lifecycleOwner = this@PlayerActivity
            setOnClick { behavior.state = BottomSheetBehavior.STATE_EXPANDED }
        }

        if (!playerAdapter.hadAdd(BANGUMI_DETAIL)) {
            playerAdapter.addBangumiDetail(bangumiDetailItemBinding.root)
            bangumiDetailItemBinding.followButton.setOnClickListener { view ->
                viewModel.onFollowButtonClick(!view.isSelected)
            }
        }
    }

    private fun bindSpainner(lineNames: Array<String?>) {
        val spinner = jiSelectItemBinding.spinner
        val spinnerAdapter = ArrayAdapter<String>(
            this@PlayerActivity,
            R.layout.spinner_item, lineNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.itemSelect { position -> viewModel.currentLine = position }
        jiSelectItemBinding.spinnerIcon.setOnClickListener {
            spinner.performClick()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val layoutParams = mVideoView.layoutParams
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true
            if ((mVideoView as ParallaxVideoView).offset != 0) (mVideoView as ParallaxVideoView).offset =
                0
            registerSensor()
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            unRegisterSensor()
            isLandscape = false
            layoutParams.height = videoViewHeight
        }
        hideStateBar()
        mReceiver.groupValue.putBoolean(FULL_SCREEN_KEY, isLandscape, true)
    }


    private fun showStateBar() {
        if (!isLandscape) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            binding.barLayout.isGone(false)
        }
        //显示导航栏
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun hideStateBar() {
        var windowState = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        if (isLandscape) { //全屏隐藏导航栏
            windowState = windowState or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }

        binding.barLayout.isGone(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = windowState
    }

    private fun registerSensor() {
        val sensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager?.registerListener(
            mSensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }


    private fun unRegisterSensor() {
        mSensorManager?.unregisterListener(mSensorEventListener)
    }

    private val mSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent?) {
            if (isLandscape)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val success =
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                mReceiver.groupValue.putBoolean(PERMISSION_REQUEST_KEY, success, true)
            }
        }
    }

    override fun onBackPressed() {
        when {
            isLandscape -> setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            behavior.state == BottomSheetBehavior.STATE_EXPANDED ->
                behavior.state = BottomSheetBehavior.STATE_HIDDEN

            else -> super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (mVideoView.state == IPlayer.STATE_STOPPED) {
            mVideoView.rePlay(currentPosition)
        } else if (mVideoView.isInPlaybackState) {
            if (!isUserPause) {
                mVideoView.resume()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isActive = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (mVideoView.isInPlaybackState) {
            if (mVideoView.isPlaying) {
                mVideoView.pause()
                isUserPause = false
            }
        }
    }

    private fun setOrientation(orientation: Int) {
        isLandscape = orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = orientation
    }
}

private inline fun Spinner.itemSelect(crossinline action: (Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            action(position)
        }

    }
}

