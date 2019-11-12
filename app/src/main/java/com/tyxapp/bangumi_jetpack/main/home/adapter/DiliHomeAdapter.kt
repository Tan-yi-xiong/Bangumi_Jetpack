package com.tyxapp.bangumi_jetpack.main.home.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.main.home.DiliUpdateBangumiBottomSheet
import com.tyxapp.bangumi_jetpack.utilities.isGone

const val BODY1_VIEW_TYPE = 2

class DilidiliHomeAdapter(
    private val mActivity: AppCompatActivity
) : DefaultNetWorkBangumiAdapter() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == BODY1_VIEW_TYPE) {
            holder as ViewHolder
            val recyclerView = holder.bind.recyclerView
            val root = holder.bind.root
            val body1Key = homeBangumis.keys.elementAt(0)
            var adapter = recyclerView.adapter
            holder.bind.homebangumiTitle.setOnClickListener {
                DiliUpdateBangumiBottomSheet.getInstance().show(
                    mActivity.supportFragmentManager,
                    DiliUpdateBangumiBottomSheet::class.java.simpleName
                )
            }

            holder.bind.title = body1Key
            if (adapter == null) {
                root.alpha = 0f
                root.post {
                    root.bodyAnimation(position)
                }
                adapter = DiliUpdateBangumiItemAdapter()
                recyclerView.layoutManager =
                    LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
                recyclerView.adapter = adapter
            }
            (adapter as DiliUpdateBangumiItemAdapter).submitList(homeBangumis[body1Key])
        } else {
            super.onBindViewHolder(holder, position)
            if (position == 3) { // 随机番剧没有更多
                holder as ViewHolder
                holder.bind.icon.isGone(true)
                holder.bind.homebangumiTitle.setOnClickListener(null)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> BANNER_VIEWTYPE
            1 -> BODY1_VIEW_TYPE
            else -> BODY_VIEWTYPE
        }
    }
}