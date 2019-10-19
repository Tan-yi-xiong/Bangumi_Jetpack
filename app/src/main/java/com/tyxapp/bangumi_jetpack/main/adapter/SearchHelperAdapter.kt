package com.tyxapp.bangumi_jetpack.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.SearchWord
import com.tyxapp.bangumi_jetpack.databinding.SearchHelperItemBinding
import com.tyxapp.bangumi_jetpack.main.viewmodels.SearchHelperViewModel
import com.tyxapp.bangumi_jetpack.utilities.info

class SearchHelperAdapter(
    private val viewModel: SearchHelperViewModel
) : ListAdapter<SearchWord, SearchHelperViewHolder>(SearchHelperDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHelperViewHolder {
        return SearchHelperViewHolder(
            SearchHelperItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            viewModel
        )
    }

    override fun onBindViewHolder(holder: SearchHelperViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

}


class SearchHelperViewHolder(
    private val binding: SearchHelperItemBinding,
    private val viewModel: SearchHelperViewModel
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(searchWord: SearchWord) {
        binding.searchWord = searchWord
        binding.viewModle = viewModel
        //数据库的条目才能长按
        if (searchWord.isFromNet) {
            binding.root.setOnLongClickListener(null)
        } else {
            binding.root.setOnLongClickListener {
                viewModel.onSearchHelperItemLongClick(searchWord)
                true
            }
        }
    }
}

private class SearchHelperDiffUtil : DiffUtil.ItemCallback<SearchWord>() {
    override fun areItemsTheSame(oldItem: SearchWord, newItem: SearchWord): Boolean =
        oldItem.word == newItem.word

    override fun areContentsTheSame(oldItem: SearchWord, newItem: SearchWord): Boolean =
        oldItem == newItem

}