package com.tyxapp.bangumi_jetpack.main.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.NetWordState
import com.tyxapp.bangumi_jetpack.databinding.ItemSearchBangumiBinding
import com.tyxapp.bangumi_jetpack.databinding.NetwordstateViewholderLayoutBinding
import com.tyxapp.bangumi_jetpack.databinding.TimetableBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DIFF_CALLBACK
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.home.adapter.TimeTableBangumiAdapter
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity

class SearchResultChildAdapter(
    private val retry: () -> Unit
) : PagedListAdapter<Bangumi, RecyclerView.ViewHolder>(BANGUMI_DIFF_CALLBACK) {
    private var netWordState: NetWordState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.networdstate_viewholder_layout -> NetWordStateViewHolder.creat(
                parent,
                retry
            )

            R.layout.timetable_bangumi_item -> SearchResultChildViewHolder(
                ItemSearchBangumiBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw IllegalAccessException("no ThisType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (hasShowBottmRow()) {
            if (position == itemCount - 1) {
                return (holder as NetWordStateViewHolder).bind(netWordState!!)
            }
        }
        (holder as SearchResultChildViewHolder).bind(getItem(position))
    }

    fun submitNetWordState(newNetWordState: NetWordState) {
        if (newNetWordState != netWordState) {
            val hadShowBottmRow = hasShowBottmRow()
            netWordState = newNetWordState
            val hasShowBottmRow = hasShowBottmRow()
            if (hasShowBottmRow) {
                if (hadShowBottmRow) {
                    notifyItemChanged(itemCount - 1)
                } else {
                    notifyItemInserted(super.getItemCount())
                }
            } else {
                if (hadShowBottmRow) {
                    notifyItemRemoved(super.getItemCount())
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (hasShowBottmRow()) {
            if (position == itemCount - 1) {
                return R.layout.networdstate_viewholder_layout
            }
        }
        return R.layout.timetable_bangumi_item
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasShowBottmRow()) 1 else 0
    }

    private fun hasShowBottmRow() = netWordState != null && netWordState != NetWordState.SUCCESS

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(TimeTableBangumiAdapter.ItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

}

class NetWordStateViewHolder(
    private val retry: () -> Unit,
    private val binding: NetwordstateViewholderLayoutBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(netWordState: NetWordState) {
        binding.showLoading = netWordState == NetWordState.LOADING
        binding.retryButton.setOnClickListener {
            retry.invoke()
        }
    }

    companion object {
        fun creat(viewGroup: ViewGroup, retry: () -> Unit): NetWordStateViewHolder {
            return NetWordStateViewHolder(
                retry,
                NetwordstateViewholderLayoutBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false
                )
            )
        }
    }
}

class SearchResultChildViewHolder(
    val bind: ItemSearchBangumiBinding
) : RecyclerView.ViewHolder(bind.root) {

    init {
        bind.setOnClick { view ->
            bind.bangumi?.let {
                (view.context as MainActivity).startPlayerActivity(it.id, it.source.name)
            }
        }
    }

    fun bind(bangumi: Bangumi?) {
        bangumi?.let { bind.bangumi = it }
        bind.executePendingBindings()
    }
}