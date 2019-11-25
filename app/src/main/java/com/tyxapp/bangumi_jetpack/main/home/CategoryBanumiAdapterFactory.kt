package com.tyxapp.bangumi_jetpack.main.home

import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.main.home.adapter.DiliCategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.home.adapter.ZfCategoryBanumiAdapter
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.CategoryBangumisViewModel
import java.lang.IllegalArgumentException

class CategoryBanumiAdapterFactory {
    companion object {
        fun getAdapter(sourceName: String, viewModel: CategoryBangumisViewModel): RecyclerView.Adapter<out RecyclerView.ViewHolder> {
            return when (sourceName) {
                BangumiSource.DiliDili.name -> DiliCategoryBanumiAdapter(viewModel)

                BangumiSource.Zzzfun.name -> ZfCategoryBanumiAdapter(viewModel)

                else -> throw IllegalArgumentException("$sourceName 没有适配器")
            }
        }
    }

}