package com.tyxapp.bangumi_jetpack.main.mydownload.adapter

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.DownLoadInfo
import com.tyxapp.bangumi_jetpack.data.DownLoadState.*
import com.tyxapp.bangumi_jetpack.data.DownloadProgress
import com.tyxapp.bangumi_jetpack.databinding.LayoutDownloadDetailItemBinding
import com.tyxapp.bangumi_jetpack.download.DownloadManager
import com.tyxapp.bangumi_jetpack.download.OnProgressUpdateListener
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels.DownloadDetailViewModel
import com.tyxapp.bangumi_jetpack.utilities.formatFileSize
import com.tyxapp.bangumi_jetpack.utilities.navigateToLoaclPlayerActivity
import com.tyxapp.bangumi_jetpack.utilities.toPx

class DownloadDetailAdapter(
    private val viewModel: DownloadDetailViewModel
) : ListAdapter<DownLoadInfo, DownloadDetailAdapter.ViewHolder>(
    DIFF_CALLBACK
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_download_detail_item, parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(ItemDecoration)
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ViewHolder(
        private val bind: LayoutDownloadDetailItemBinding,
        private val viewModel: DownloadDetailViewModel
    ) : RecyclerView.ViewHolder(bind.root) {

        @Suppress("UNCHECKED_CAST")
        @SuppressLint("SetTextI18n")
        fun bind(downLoadInfo: DownLoadInfo) {
            with(bind) {
                this.downLoadInfo = downLoadInfo
                fileTotal.text = "${formatFileSize(downLoadInfo.currentPosition)}/${formatFileSize(downLoadInfo.total)}"
                if (root.tag != null) {
                    DownloadManager.removeOnProgressUpdateListener(bind.root.tag as OnProgressUpdateListener)
                    root.tag = null
                }

                stateButton.isSelected = true
                stateButton.visibility = View.VISIBLE
                root.setOnLongClickListener {
                    viewModel.onitemLongClick(downLoadInfo.id)
                    true
                }
            }

            bind.root.setOnClickListener {
                if (downLoadInfo.state == FINISH) {
                    (it.context as MainActivity).navigateToLoaclPlayerActivity(downLoadInfo.filePath)
                }
            }

            when(downLoadInfo.state) {
                FAILD -> {
                    bind.downloadState.text = "下载出错"
                    bind.stateButton.setOnClickListener {
                        DownloadManager.start(downLoadInfo)
                    }
                }

                PUASE -> {
                    bind.downloadState.text = "下载暂停"
                    bind.stateButton.setOnClickListener {
                        DownloadManager.startNow(downLoadInfo.id)
                    }
                }

                WAIT -> {
                    bind.downloadState.text = "等待中"
                    bind.stateButton.setOnClickListener {
                        DownloadManager.startNow(downLoadInfo.id)
                    }
                }

                FINISH -> {
                    bind.downloadState.text = "下载完成"
                    bind.stateButton.visibility = View.INVISIBLE
                }

                DOWNLOADING -> {
                    bind.downloadState.text = "下载中.."
                    bind.stateButton.isSelected = false
                    bind.stateButton.setOnClickListener {
                        DownloadManager.puase(downLoadInfo.id)
                    }
                    val onProgressUpdateListener = { downloadProgress: DownloadProgress ->
                        if (downloadProgress.id == downLoadInfo.id) {
                            bind.progressBar.max = downloadProgress.total.toInt()
                            bind.progressBar.progress = downloadProgress.progress.toInt()
                            bind.fileTotal.text = "${formatFileSize(downloadProgress.progress)}/${formatFileSize(downloadProgress.total)}"
                        }
                    }
                    DownloadManager.addOnProgressUpdateListener(onProgressUpdateListener)
                    bind.root.tag = onProgressUpdateListener
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DownLoadInfo>() {
            override fun areItemsTheSame(oldItem: DownLoadInfo, newItem: DownLoadInfo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DownLoadInfo, newItem: DownLoadInfo): Boolean {
                return oldItem == newItem
            }

        }

        val ItemDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.bottom = 8.toPx()
            }
        }
    }
}
