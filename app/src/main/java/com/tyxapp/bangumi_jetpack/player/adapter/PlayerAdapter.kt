package com.tyxapp.bangumi_jetpack.player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.State
import com.tyxapp.bangumi_jetpack.data.UIStata
import com.tyxapp.bangumi_jetpack.databinding.PlayerUistaetLayoutBinding
import org.jetbrains.anko.toast

const val BANGUMI_DETAIL = 0
const val JI_SELECT = 1
const val RECOMMEND = 2

private const val UI_STATE = 3

class PlayerAdapter(
    private val retry: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var hadAddItemCount = 0
    private var mUIState: UIStata? = null
    private val itemViews = arrayOfNulls<View>(3)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType != UI_STATE) {
            object : RecyclerView.ViewHolder(itemViews[viewType]!!){}
        } else {
            UIStateViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.player_uistaet_layout, parent, false
                ),
                retry
            )
        }
    }

    override fun getItemCount(): Int {
        if (hadAddItemCount <= 3){
            hadAddItemCount
        } else {
            throw IllegalAccessException("最多只能有3个条目")
        }
        return hadAddItemCount + if (hasShowUIState()) 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == UI_STATE) {
            (holder as UIStateViewHolder).bind(mUIState!!)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.itemAnimator = ItemAnimation()
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasShowUIState() && position == itemCount - 1) {
            UI_STATE
        } else {
            position
        }
    }

    fun submitUIState(uiStata: UIStata) {
        //三部分添加完还通知uiError一般为视频url加载出错
        if (hadAddItemCount == 3 && uiStata.netWordState.state == State.ERROR) {
            BangumiApp.getContext().toast(uiStata.netWordState.msg ?: "发生未知错误")
            return
        }

        val hadShowUIState = hasShowUIState()
        mUIState = uiStata
        val hasShowUIState = hasShowUIState()
        if (hasShowUIState) {
            if (hadShowUIState) {
                notifyItemChanged(itemCount - 1)
            } else {
                notifyItemInserted(itemCount)
            }
        } else {
            if (hadShowUIState) {
                notifyItemRemoved(itemCount - 1)
            }
        }
    }

    private fun hasShowUIState() = mUIState != null && mUIState != UIStata.SUCCESS

    fun addBangumiDetail(view: View) {
        checkItemAdd("BangumiDetail已添加", BANGUMI_DETAIL)
        itemViews[BANGUMI_DETAIL] = view
        hadAddItemCount++
        notifyItemInserted(itemCount)
    }

    fun addJiSelectView(view: View) {
        checkItemAdd("JiSelectView已添加", 1)
        itemViews[JI_SELECT] = view
        hadAddItemCount++
        notifyItemInserted(itemCount)
    }

    fun addRecommendView(view: View) {
        checkItemAdd("RecommendView已添加", 2)
        itemViews[RECOMMEND] = view
        hadAddItemCount++
        notifyItemInserted(itemCount)
    }

    fun hadAdd(viewPosition: Int) = itemViews[viewPosition] != null

    private fun checkItemAdd(msg: String, position: Int) {
        itemViews[position]?.let {
            throw IllegalAccessException(msg)
        }
    }


}

private class UIStateViewHolder(
    val bind: PlayerUistaetLayoutBinding,
    private val retry: () -> Unit
) : RecyclerView.ViewHolder(bind.root) {

    fun bind(uiStata: UIStata) {
        when (uiStata) {
            UIStata.LOADING -> {
                bind.showContent = false
                bind.showLoading = true
            }

            UIStata.DATA_EMPTY -> {
                bind.showLoading = false
                bind.showContent = true
                bind.tipsText = "没有解析到集数"
            }

            else -> if (uiStata.netWordState.state == State.ERROR) {
                bind.showContent = true
                bind.showLoading = false
                bind.tipsText = "解析出错了!!!"
                bind.retryButton.setOnClickListener { retry() }
            }
        }
    }
}

