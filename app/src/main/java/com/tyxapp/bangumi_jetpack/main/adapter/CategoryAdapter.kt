package com.tyxapp.bangumi_jetpack.main.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.CategorItem
import com.tyxapp.bangumi_jetpack.databinding.CategoryItemBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.navigateToCategoryBangumisFragment
import com.tyxapp.bangumi_jetpack.utilities.popAnimation
import com.tyxapp.bangumi_jetpack.utilities.toPx

class CategoryAdapter: ListAdapter<CategorItem, CategoryViewHolder>(
    CategoryDiffCallBack()
) {

    private var lastPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.category_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
        //逐条执行pop动画
        if (lastPosition <= position) {
            holder.bind.cover.popAnimation((50 * position).toLong())
            lastPosition = position
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
        recyclerView.addItemDecoration(ItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

}

class CategoryViewHolder(
        val bind: CategoryItemBinding
) : RecyclerView.ViewHolder(bind.root) {

    fun bind(categorItem: CategorItem) {
        with(bind) {
            this.categorItem = categorItem

            //暂时没有想到更好的办法
            bind.cover.setOnClickListener { view ->
                (view.context as MainActivity).navigateToCategoryBangumisFragment(categorItem.categorName)
            }
            executePendingBindings()
        }
    }
}

private class CategoryDiffCallBack : DiffUtil.ItemCallback<CategorItem>() {
    override fun areItemsTheSame(oldItem: CategorItem, newItem: CategorItem) = oldItem.categorName == newItem.categorName

    override fun areContentsTheSame(oldItem: CategorItem, newItem: CategorItem) = oldItem == newItem

}

private class ItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position < 3) {
            outRect.top = 20.toPx()
        }
    }
}