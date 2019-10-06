package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.BannerItemBinding
import com.tyxapp.bangumi_jetpack.main.viewmodels.NetWorkBangumiViewModel
import com.tyxapp.bangumi_jetpack.utilities.toPx

class BannerAdapter : ListAdapter<Bangumi, BannerViewHolder>(BangumiDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.banner_item, parent, false))
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(getItem(position), "${position + 1}/$itemCount")
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
        recyclerView.addItemDecoration(ItemDecoration())
        PagerSnapHelper().attachToRecyclerView(recyclerView)
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.left = 13.toPx()
            }
            outRect.top = 8.toPx()
            outRect.right = 16.toPx()
        }
    }

}

class BannerViewHolder(
        private val bind: BannerItemBinding
) : RecyclerView.ViewHolder(bind.root) {

    fun bind(bangumi: Bangumi, indicatorText: String) {
        bind.bangumi = bangumi
        bind.indicator.text = indicatorText
    }
}


