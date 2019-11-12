package com.tyxapp.bangumi_jetpack.main.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.databinding.HomeNetworkBangumiItemBinding
import com.tyxapp.bangumi_jetpack.main.MainActivity
import com.tyxapp.bangumi_jetpack.utilities.navigateToCategoryBangumisFragment
import kotlin.collections.set


const val BANNER_VIEWTYPE = 0
const val BODY_VIEWTYPE = 1

open class DefaultNetWorkBangumiAdapter : INetWorkBangumiAdapter<RecyclerView.ViewHolder>() {
    internal var homeBangumis = LinkedHashMap<String, List<Bangumi>>()

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
                recyclerView.alpha = 0f
                recyclerView.post {
                    recyclerView.bannerAnimation()
                }
                recyclerView.adapter = BannerAdapter()
                    .apply { submitList(homeBangumis[BANNER]) }
            } else {
                (recyclerView.adapter as BannerAdapter).submitList(homeBangumis[BANNER])
            }

        } else {
            holder as ViewHolder
            val title = homeBangumis.keys.elementAt(position - 1)
            holder.bind(title, homeBangumis[title], position)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun submitHomeData(map: Map<String, List<Bangumi>>) {
        homeBangumis.clear()
        homeBangumis.putAll(map)
        //把轮播集合放到最后, 方便其他集合按顺序获取
        val bannerBangumis = homeBangumis.remove(BANNER) ?: throw NullPointerException("轮播条目不能为空")
        homeBangumis[BANNER] = bannerBangumis

        notifyDataSetChanged()
    }


    class ViewHolder(
        val bind: HomeNetworkBangumiItemBinding
    ) : RecyclerView.ViewHolder(bind.root) {

        fun bind(title: String, bangumis: List<Bangumi>?, position: Int) {
            bind.title = title

            bind.homebangumiTitle.setOnClickListener { view ->
                (view.context as MainActivity).navigateToCategoryBangumisFragment(title)
            }

            val recyclerView = bind.recyclerView
            if (recyclerView.adapter == null) {
                bind.root.alpha = 0f
                bind.root.post {
                    bind.root.bodyAnimation(position)
                }

                recyclerView.adapter = BangumiAdapter()
            }
            (recyclerView.adapter as BangumiAdapter).submitList(bangumis)
            bind.executePendingBindings()
        }
    }
}

private fun RecyclerView.bannerAnimation() {
    this.alpha = 0f
    this.translationX = width.toFloat() / 2
    animate().alpha(1f)
        .translationX(0f)
        .setInterpolator(LinearOutSlowInInterpolator())
        .setDuration(400)
        .start()
}

internal fun View.bodyAnimation(position: Int) {
    alpha = 0f
    translationY = height.toFloat() / 2
    animate().alpha(1f)
        .translationY(0f)
        .setDuration(400)
        .setInterpolator(LinearOutSlowInInterpolator())
        .setStartDelay((position * 50).toLong())
        .start()
}



