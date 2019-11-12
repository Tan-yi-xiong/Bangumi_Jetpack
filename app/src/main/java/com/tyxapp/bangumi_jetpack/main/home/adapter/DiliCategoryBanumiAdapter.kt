package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.DiliBangumi
import com.tyxapp.bangumi_jetpack.databinding.LayoutDiliCategoryBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.player.adapter.ItemAnimation
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity

class DiliCategoryBanumiAdapter: ListAdapter<DiliBangumi, DiliCategoryBanumiAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_dili_category_bangumi_item, parent, false
            )
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


    class ViewHolder(
        private val bind: LayoutDiliCategoryBangumiItemBinding
    ) : RecyclerView.ViewHolder(bind.root) {

        fun bind(diliBangumi: DiliBangumi) {
            bind.diliBangumi = diliBangumi
            bind.root.setOnClickListener {
                (it.context as MainActivity).startPlayerActivity(diliBangumi.id, diliBangumi.source.name)
            }
        }
    }

    private class DiffCallback: DiffUtil.ItemCallback<DiliBangumi>() {
        override fun areItemsTheSame(oldItem: DiliBangumi, newItem: DiliBangumi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiliBangumi, newItem: DiliBangumi): Boolean {
            return newItem == oldItem
        }

    }
}