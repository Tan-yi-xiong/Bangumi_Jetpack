package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.CategoryBangumi
import com.tyxapp.bangumi_jetpack.databinding.ZfCategoryBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.CategoryBangumisViewModel
import com.tyxapp.bangumi_jetpack.utilities.toPx
import com.tyxapp.bangumi_jetpack.views.CheckableImageButton

class ZfCategoryBanumiAdapter(
    private val viewModel: CategoryBangumisViewModel
) : PagedListAdapter<CategoryBangumi, CategoryDetailViewHoder>(DIFF_CALLBACK), ICategoryAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailViewHoder {
        return CategoryDetailViewHoder(
            ZfCategoryBangumiItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: CategoryDetailViewHoder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onFollowStateChange(position: Int, isFollow: Boolean) {
        getItem(position)?.let {
            it.isFollow = isFollow
            notifyItemChanged(position)
        }
    }

    override fun getBagnumi(position: Int): CategoryBangumi? {
        return getItem(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
        recyclerView.addItemDecoration(CategoryBangumiItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryBangumi>() {
            override fun areItemsTheSame(
                oldItem: CategoryBangumi,
                newItem: CategoryBangumi
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CategoryBangumi,
                newItem: CategoryBangumi
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

}

class CategoryDetailViewHoder(
    private val bind: ZfCategoryBangumiItemBinding,
    private val viewModel: CategoryBangumisViewModel
) : RecyclerView.ViewHolder(bind.root) {

     init {
        bind.setOnClick { view ->
            when (view.id) {
                R.id.item_root -> {
                    bind.bangumi?.let {
                        viewModel.itemClickPosition.value = adapterPosition
                    }
                }

                R.id.follow_button -> {
                    (view as CheckableImageButton).toggle()
                    bind.bangumi?.let {
                        viewModel.bangumiFollowStateChange(it, view.isChecked)
                        it.isFollow = view.isChecked
                    }
                }
            }
        }
    }

    fun bind(bangumi: CategoryBangumi?) {
        with(bind) {
            this.bangumi = bangumi
            followButton.isChecked = bangumi?.isFollow ?: false
            executePendingBindings()
        }
    }
}

    class CategoryBangumiItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            //第一行头部要留出些空位
            val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
            val position = parent.getChildAdapterPosition(view)
            if (position in 0 until spanCount) {
                outRect.top = 16.toPx()
            }
        }
    }