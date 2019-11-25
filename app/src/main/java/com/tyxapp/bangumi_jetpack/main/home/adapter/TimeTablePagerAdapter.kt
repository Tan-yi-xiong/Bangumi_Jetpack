package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.Bangumi

class TimeTablePagerAdapter : ListAdapter<List<Bangumi>, TimeTablePagerAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return ViewHolder(RecyclerView(parent.context).apply {
            this.layoutParams = layoutParams
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val mRecyclerView: RecyclerView
    ) : RecyclerView.ViewHolder(mRecyclerView) {

        fun bind(bangumis: List<Bangumi>) {
            // 复用时对于有adapter的就不用再创建了
            if (mRecyclerView.adapter == null) {
                mRecyclerView.adapter = TimeTableBangumiAdapter()
            }
            (mRecyclerView.adapter  as TimeTableBangumiAdapter).submitList(bangumis)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<List<Bangumi>>() {
            override fun areItemsTheSame(oldItem: List<Bangumi>, newItem: List<Bangumi>): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: List<Bangumi>,
                newItem: List<Bangumi>
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

}