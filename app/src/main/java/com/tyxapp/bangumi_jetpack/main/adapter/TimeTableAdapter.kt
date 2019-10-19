package com.tyxapp.bangumi_jetpack.main.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.TimetableBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.viewmodels.TimeTableViewModel
import com.tyxapp.bangumi_jetpack.utilities.toPx

class TimeTableAdapter(
        private val viewModel: TimeTableViewModel
) : ListAdapter<Bangumi, TimeTableViewHolder>(BangumiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeTableViewHolder {
        return TimeTableViewHolder(
            TimetableBangumiItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),

            viewModel
        )
    }

    override fun onBindViewHolder(holder: TimeTableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(ItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.bottom = 10.toPx()
        }
    }

}

class TimeTableViewHolder(
        private val bind: TimetableBangumiItemBinding,
        private val viewModel: TimeTableViewModel
) : RecyclerView.ViewHolder(bind.root) {

    fun bind(bangumi: Bangumi) {
        with(bind) {
            this.bangumi = bangumi
            this.viewmodel = viewModel
        }
    }
}
