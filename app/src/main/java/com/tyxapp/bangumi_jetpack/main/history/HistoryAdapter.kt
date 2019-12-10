package com.tyxapp.bangumi_jetpack.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.databinding.BangumiFollowHistoryItemBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DETAIL_DIFFUTIL
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val viewModel: HistoryViewModel
) : PagedListAdapter<BangumiDetail, HistoryBagnuimisViewHolder>(
    BANGUMI_DETAIL_DIFFUTIL
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryBagnuimisViewHolder {
        return HistoryBagnuimisViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.bangumi_follow_history_item, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: HistoryBagnuimisViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

}

class HistoryBagnuimisViewHolder(
    private val binding: BangumiFollowHistoryItemBinding,
    private val viewModel: HistoryViewModel
) : RecyclerView.ViewHolder(binding.root) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    init {
        binding.root.setOnLongClickListener {
            binding.bangumi?.let { bangumiDetail ->
                viewModel.onHistoryItemLongClick(bangumiDetail)
            }
            true
        }

        binding.root.setOnClickListener { view ->
            binding.bangumi?.let {
                (view.context as MainActivity).startPlayerActivity(it.id, it.source.name)
            }
        }
    }

    fun bind(bangumiDetail: BangumiDetail?) {
        bangumiDetail?.let {
            binding.bangumi = it
            binding.historyTime.text = dateFormat.format(it.lastWatchTime)
        }
    }
}