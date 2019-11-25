package com.tyxapp.bangumi_jetpack.main.mydownload.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.BangumiDetail
import com.tyxapp.bangumi_jetpack.databinding.DownoadBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.BANGUMI_DETAIL_DIFFUTIL
import com.tyxapp.bangumi_jetpack.main.home.adapter.CategoryBangumiItemDecoration
import com.tyxapp.bangumi_jetpack.main.mydownload.MyDownloadFragmentDirections
import com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels.MyDownloadViewModel

class DownloadAdapter(
    private val viewModel: MyDownloadViewModel
) : ListAdapter<BangumiDetail, DownloadAdapter.ViewHolder>(
    BANGUMI_DETAIL_DIFFUTIL
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.zf_category_bangumi_item, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 3)
        recyclerView.addItemDecoration(CategoryBangumiItemDecoration())
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ViewHolder(
        private val bind: DownoadBangumiItemBinding,
        private val viewModel: MyDownloadViewModel
    ) : RecyclerView.ViewHolder(bind.root) {

        fun bind(bangumiDetail: BangumiDetail) {
            bind.bangumiDetail = bangumiDetail
            bind.root.setOnClickListener { view ->
                val action =
                    MyDownloadFragmentDirections.actionMyDownloadFragmentToDownloadDetailFragment(
                        bangumiDetail.id, bangumiDetail.source.name
                    )
                view.findNavController().navigate(action)
            }
            bind.root.setOnLongClickListener {
                viewModel.onItemLongCick(bangumiDetail)
                true
            }
        }
    }
}
