package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.CategoryBangumiItemBinding
import com.tyxapp.bangumi_jetpack.utilities.toPx

class CategoryBanumiAdapter :
    PagedListAdapter<Bangumi, CategoryDetailViewHoder>(BangumiDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailViewHoder {
        return CategoryDetailViewHoder(
            CategoryBangumiItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryDetailViewHoder, position: Int) =
        holder.bind(getItem(position))

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
        recyclerView.addItemDecoration(CategoryBangumiItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

}


class CategoryDetailViewHoder(
    private val bind: CategoryBangumiItemBinding
) : RecyclerView.ViewHolder(bind.root) {
    fun bind(bangumi: Bangumi?) = with(bind) {
        this.bangumi = bangumi
        bind.executePendingBindings()
    }
}

private class CategoryBangumiItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        //第一行头部要留出些空位, 不然和toolbar连在一起
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
        val position = parent.getChildAdapterPosition(view)
        if (position in 0 until spanCount) {
            outRect.top = 16.toPx()
        }
    }
}