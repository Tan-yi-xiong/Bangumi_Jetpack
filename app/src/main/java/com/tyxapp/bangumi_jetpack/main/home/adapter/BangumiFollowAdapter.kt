package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.databinding.BangumiFollowHistoryItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DETAIL_DIFFUTIL
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.BangumiFollowViewModel
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity


class BangumiFollowAdapter(
    private val viewModel: BangumiFollowViewModel
) : PagedListAdapter<BangumiDetail, BangumiFollowViewHolder>(BANGUMI_DETAIL_DIFFUTIL) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BangumiFollowViewHolder {
        return BangumiFollowViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.bangumi_follow_history_item, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: BangumiFollowViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

}

class BangumiFollowViewHolder(
    private val bind: BangumiFollowHistoryItemBinding,
    private val viewModel: BangumiFollowViewModel
) : RecyclerView.ViewHolder(bind.root) {

    init {
        bind.root.setOnLongClickListener { view ->
            bind.bangumi?.let {
                viewModel.onItemLongClick(it)
            }
            false
        }

        bind.root.setOnClickListener { view ->
            bind.bangumi?.let {
                (view.context as MainActivity).startPlayerActivity(it.id, it.source.name)
            }
        }
    }

    fun bind(bangumiDetail: BangumiDetail?) {
        bangumiDetail?.let {
            bind.bangumi = it
            bind.root.setOnClickListener {
            }
        }

    }
}
