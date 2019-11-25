package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.CategoryBangumi
import com.tyxapp.bangumi_jetpack.data.DiliBangumi
import com.tyxapp.bangumi_jetpack.databinding.LayoutDiliCategoryBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.CategoryBangumisViewModel
import com.tyxapp.bangumi_jetpack.player.adapter.ItemAnimation
import com.tyxapp.bangumi_jetpack.views.CheckableImageButton

class DiliCategoryBanumiAdapter(
    private val viewModel: CategoryBangumisViewModel
) : ListAdapter<DiliBangumi, DiliCategoryBanumiAdapter.ViewHolder>(DiffCallback()), ICategoryAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_dili_category_bangumi_item, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.itemAnimator = ItemAnimation()
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onFollowStateChange(position: Int, isFollow: Boolean) {
        getItem(position).isFollow = isFollow
        notifyItemChanged(position)
    }

    override fun getBagnumi(position: Int): CategoryBangumi? {
        return getItem(position)
    }


    class ViewHolder(
        private val bind: LayoutDiliCategoryBangumiItemBinding,
        private val viewModel: CategoryBangumisViewModel
    ) : RecyclerView.ViewHolder(bind.root) {

        init {
            bind.setOnClick { view ->
                when (view.id) {
                    R.id.follow_button -> {
                        (view as CheckableImageButton).toggle()
                        bind.diliBangumi?.let {
                            viewModel.bangumiFollowStateChange(it, view.isChecked)
                            it.isFollow = view.isChecked
                        }
                    }

                    R.id.item_parent -> {
                        bind.diliBangumi?.let {
                            viewModel.itemClickPosition.value = adapterPosition
                        }
                    }
                }
            }
        }

        fun bind(diliBangumi: DiliBangumi) {
            with(bind) {
                this.diliBangumi = diliBangumi
                bind.followButton.isChecked = diliBangumi.isFollow
                executePendingBindings()
            }

        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DiliBangumi>() {
        override fun areItemsTheSame(oldItem: DiliBangumi, newItem: DiliBangumi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiliBangumi, newItem: DiliBangumi): Boolean {
            return newItem == oldItem
        }

    }
}