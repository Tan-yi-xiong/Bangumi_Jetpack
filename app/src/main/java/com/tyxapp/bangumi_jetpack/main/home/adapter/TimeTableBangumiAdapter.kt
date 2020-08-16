package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.TimeTableBangumi
import com.tyxapp.bangumi_jetpack.databinding.TimetableBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DIFF_CALLBACK
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.startPlayerActivity
import com.tyxapp.bangumi_jetpack.utilities.toPx

class TimeTableBangumiAdapter : ListAdapter<TimeTableBangumi, TimeTableViewHolder>(TimeTableBangumi.ItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeTableViewHolder {
        return TimeTableViewHolder(
            TimetableBangumiItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
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
        private val bind: TimetableBangumiItemBinding
) : RecyclerView.ViewHolder(bind.root) {

    init {
        bind.setOnClick { view ->
            bind.bangumi?.let {
                (view.context as MainActivity).startPlayerActivity(it.id, it.source.name)
            }
        }
    }

    fun bind(bangumi: TimeTableBangumi) {
        with (bind) {
            this.bangumi = bangumi
            executePendingBindings()
        }
    }
}
