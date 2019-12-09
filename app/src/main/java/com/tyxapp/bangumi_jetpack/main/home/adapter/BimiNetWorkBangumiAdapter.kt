package com.tyxapp.bangumi_jetpack.main.home.adapter

import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView

class BimiNetWorkBangumiAdapter : DefaultNetWorkBangumiAdapter() {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 1) {
            (holder as ViewHolder)
            holder.bind.homebangumiTitle.setOnClickListener(null)
            holder.bind.icon.isGone = true
        }
    }
}