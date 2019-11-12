package com.tyxapp.bangumi_jetpack.player.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.JiItem
import com.tyxapp.bangumi_jetpack.databinding.JiSelectItemBinding
import com.tyxapp.bangumi_jetpack.player.PlayerViewModel

class JiSelectAdapter(
    private val viewModel: PlayerViewModel
) : ListAdapter<JiItem, JiSelectViewHolder>(DIFF_UTIL) {

    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JiSelectViewHolder {
        return JiSelectViewHolder(
            viewModel,
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.ji_select_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: JiSelectViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
        recyclerView.addItemDecoration(JiSelectItemDecoration())
        mRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    fun getCurrentJiName(): String {
        currentList.forEach {
            if (it.isSelect) {
                return it.text
            }
        }
        return ""
    }

    fun selectJi(position: Int) {
        val lastSelectPosition = findSelectItem()
        if (lastSelectPosition != -1 && lastSelectPosition != position) {
            val selectHolder = mRecyclerView.findViewHolderForLayoutPosition(position)
            val lastSelectHolder = mRecyclerView.findViewHolderForLayoutPosition(lastSelectPosition)

            //改变列表数据
            getItem(position).isSelect = true
            getItem(lastSelectPosition).isSelect = false

            //改变视图
            if (selectHolder != null) {
                selectHolder.itemView.isSelected = true
            }
            if (lastSelectHolder != null) {
                lastSelectHolder.itemView.isSelected = false
            } else {
                notifyItemChanged(lastSelectPosition)
            }
        } else if (lastSelectPosition == -1) {
            if (position >= itemCount) return
            mRecyclerView.findViewHolderForLayoutPosition(position)?.itemView?.isSelected = true
            getItem(position).isSelect = true
        }
    }

    fun findSelectItem(): Int {
        var position = -1
        for (i in 0 until itemCount) {
            if (getItem(i).isSelect) {
                position = i
                break
            }
        }
        return position
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<JiItem>() {
            override fun areItemsTheSame(oldItem: JiItem, newItem: JiItem): Boolean {
                return oldItem.text == newItem.text
            }

            override fun areContentsTheSame(oldItem: JiItem, newItem: JiItem): Boolean {
                return oldItem == newItem
            }

        }

    }
}

class JiSelectViewHolder(
    private val viewModel: PlayerViewModel,
    val bind: JiSelectItemBinding
) : RecyclerView.ViewHolder(bind.root) {

    fun bind(jiItem: JiItem, position: Int) {
        bind.jiItem = jiItem
        bind.root.setOnClickListener { viewModel.onJiClick(position) }
        bind.executePendingBindings()
    }
}

private class JiSelectItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = parent.layoutManager
        val decoration = view.context.resources.getDimension(R.dimen.player_item_margin).toInt()

        if (layoutManager is GridLayoutManager) {
            outRect.bottom = decoration
            if (position % layoutManager.spanCount == 0) {
                outRect.left = decoration
            }
        } else {
            if (position == 0) {
                outRect.left = decoration
            }
            outRect.right = decoration
        }

    }
}