package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.NetworkBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DIFF_CALLBACK
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity
import com.tyxapp.bangumi_jetpack.utilities.toPx

class BangumiAdapter : ListAdapter<Bangumi, BangumiViewHolder>(BANGUMI_DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BangumiViewHolder {
        return BangumiViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.network_bangumi_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BangumiViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
        recyclerView.addItemDecoration(ItemDecoration())
        LinearSnapHelper().attachToRecyclerView(recyclerView)
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.left = 12.toPx()
            }
            outRect.right = 10.toPx()
        }
    }
}

class BangumiViewHolder(
    private val bind: NetworkBangumiItemBinding
) : RecyclerView.ViewHolder(bind.root) {

    init {
        bind.setOnClick { view ->
            bind.bangumi?.let {
                (view.context as MainActivity).startPlayerActivity(it.id, it.source.name)
            }

        }
    }

    fun bind(bangumi: Bangumi) {
        bind.bangumi = bangumi

    }
}
