package com.tyxapp.bangumi_jetpack.player.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.RecommendBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DIFF_CALLBACK
import com.tyxapp.bangumi_jetpack.player.PlayerViewModel
import com.tyxapp.bangumi_jetpack.utilities.toPx

class RecommendAdapter(
    private val viewModel: PlayerViewModel
) : ListAdapter<Bangumi, RecommendViewHoder>(BANGUMI_DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHoder {
        return RecommendViewHoder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.recommend_bangumi_item, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: RecommendViewHoder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(ITEM_DECORATION)
        super.onAttachedToRecyclerView(recyclerView)
    }

    companion object {
        private val ITEM_DECORATION = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position != parent.adapter!!.itemCount - 1) {
                    outRect.bottom = 8.toPx()
                }
            }
        }
    }

}

class RecommendViewHoder(
    val bind: RecommendBangumiItemBinding,
    private val viewModel: PlayerViewModel
) : RecyclerView.ViewHolder(bind.root) {

    fun bind(bangumi: Bangumi) {
        bind.bangumi = bangumi
        bind.viewModel = viewModel
    }
}
