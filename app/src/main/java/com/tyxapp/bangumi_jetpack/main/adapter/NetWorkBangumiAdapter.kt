package com.tyxapp.bangumi_jetpack.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.HomeNetworkBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.navigateToCategoryBangumisFragment
import kotlin.collections.set

const val BANNER = "banner"

const val BANNER_VIEWTYPE = 0
const val BODY_VIEWTYPE = 1

class NetWorkBangumiAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var homeBangumis = LinkedHashMap<String, List<Bangumi>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        BANNER_VIEWTYPE -> object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner, parent, false)
        ) {}
        else -> {
            ViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.home_network_bangumi_item, parent, false
                )
            )
        }
    }

    override fun getItemCount(): Int = homeBangumis.size


    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> BANNER_VIEWTYPE
        else -> BODY_VIEWTYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == BANNER_VIEWTYPE) {
            val recyclerView = holder.itemView as RecyclerView
            if (recyclerView.adapter == null) {
                recyclerView.adapter = BannerAdapter()
                    .apply { submitList(homeBangumis[BANNER]) }
            } else {
                (recyclerView.adapter as BannerAdapter).submitList(homeBangumis[BANNER])
            }
        } else {
            (holder as ViewHolder).bind(homeBangumis.keys.elementAt(position - 1))
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    fun submitHomeData(map: Map<String, List<Bangumi>>) {
        homeBangumis.clear()
        homeBangumis.putAll(map)
        //把轮播集合放到最后, 方便其他集合按顺序获取
        val bannerBangumis = homeBangumis.remove(BANNER) ?: throw NullPointerException("轮播条目不能为空")
        homeBangumis[BANNER] = bannerBangumis

        notifyDataSetChanged()
    }


    inner class ViewHolder(
        private val bind: HomeNetworkBangumiItemBinding
    ) : RecyclerView.ViewHolder(bind.root) {

        fun bind(title: String) {
            bind.title = title

            //暂时想不到其他办法
            bind.homebangumiTitle.setOnClickListener { view ->
                (view.context as MainActivity).navigateToCategoryBangumisFragment(title)
            }

            val bangumis = homeBangumis[title] ?: throw IndexOutOfBoundsException("该标题没有内容")
            val recyclerView = bind.recyclerView
            if (recyclerView.adapter == null) {
                recyclerView.adapter = BangumiAdapter()
            }
            (recyclerView.adapter as BangumiAdapter).submitList(bangumis)
            bind.executePendingBindings()
        }
    }
}



