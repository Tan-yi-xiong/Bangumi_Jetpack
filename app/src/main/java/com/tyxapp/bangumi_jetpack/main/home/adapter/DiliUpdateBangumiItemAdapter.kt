package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.LayoutDidlidiliItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DIFF_CALLBACK
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity

class DiliUpdateBangumiItemAdapter: ListAdapter<Bangumi, DiliUpdateBangumiItemAdapter.ViewHolder>(BANGUMI_DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_didlidili_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(BangumiAdapter.ItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ViewHolder(
        private val bind: LayoutDidlidiliItemBinding
    ) : RecyclerView.ViewHolder(bind.root) {

        fun bind(bangumi: Bangumi) {
            bind.bangumi = bangumi
            bind.root.setOnClickListener {
                (it.context as MainActivity).startPlayerActivity(bangumi.id, bangumi.source.name)
            }
        }
    }

}

