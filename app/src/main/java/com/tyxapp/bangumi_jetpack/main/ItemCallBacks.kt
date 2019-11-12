package com.tyxapp.bangumi_jetpack.main

import androidx.recyclerview.widget.DiffUtil
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.BangumiDetail

object BANGUMI_DETAIL_DIFFUTIL : DiffUtil.ItemCallback<BangumiDetail>() {
    override fun areItemsTheSame(oldItem: BangumiDetail, newItem: BangumiDetail): Boolean {
        return oldItem.id == newItem.id && oldItem.source == newItem.source
    }

    override fun areContentsTheSame(oldItem: BangumiDetail, newItem: BangumiDetail): Boolean {
        return oldItem == newItem
    }

}

object BANGUMI_DIFF_CALLBACK : DiffUtil.ItemCallback<Bangumi>() {
    override fun areContentsTheSame(oldItem: Bangumi, newItem: Bangumi) =
        oldItem.id == newItem.id && oldItem.source == newItem.source

    override fun areItemsTheSame(oldItem: Bangumi, newItem: Bangumi) = oldItem == newItem

}