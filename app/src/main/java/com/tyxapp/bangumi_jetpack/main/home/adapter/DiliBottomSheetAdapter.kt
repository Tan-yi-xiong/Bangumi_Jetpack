package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.LayoutDiliUpdateBsItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DIFF_CALLBACK
import com.tyxapp.bangumi_jetpack.player.ARG_BANGUMI_SOURCE
import com.tyxapp.bangumi_jetpack.player.ARG_ID
import com.tyxapp.bangumi_jetpack.player.PlayerActivity
import com.tyxapp.bangumi_jetpack.utilities.toPx

class DiliBottomSheetAdapter :
    ListAdapter<Bangumi, DiliBottomSheetAdapter.ViewHolder>(BANGUMI_DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_dili_update_bs_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 2)
        recyclerView.addItemDecoration(ItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

    private class ItemDecoration: RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.layoutManager is GridLayoutManager) {
                val position = parent.getChildAdapterPosition(view)
                if (position < (parent.layoutManager as GridLayoutManager).spanCount) {
                    outRect.top = 8.toPx()
                }
            }

        }
    }

    class ViewHolder(
        private val bind: LayoutDiliUpdateBsItemBinding
    ) : RecyclerView.ViewHolder(bind.root) {

        init {
            bind.setOnClick { view ->
                bind.bangumi?.let {
                    val intent = Intent(view.context, PlayerActivity::class.java)
                    intent.putExtra(ARG_ID, it.id)
                    intent.putExtra(ARG_BANGUMI_SOURCE, it.source.name)
                    view.context.startActivity(intent)
                }
            }
        }

        fun bind(bangumi: Bangumi) {
            bind.bangumi = bangumi
            bind.cover.setOnClickListener {
                val intent = Intent(it.context, PlayerActivity::class.java)
                intent.putExtra(ARG_ID, bangumi.id)
                intent.putExtra(ARG_BANGUMI_SOURCE, bangumi.source.name)
                it.context.startActivity(intent)
            }
        }
    }
}