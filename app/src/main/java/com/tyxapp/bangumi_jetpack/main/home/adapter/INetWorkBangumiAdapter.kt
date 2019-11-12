package com.tyxapp.bangumi_jetpack.main.home.adapter

import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.data.Bangumi

const val BANNER = "banner"

/**
 * 多主页抽象类, 每个主页实现该接口给页面调用
 *
 */
abstract class INetWorkBangumiAdapter<T: RecyclerView.ViewHolder>: RecyclerView.Adapter<T>() {
     abstract fun submitHomeData(map: Map<String, List<Bangumi>>)
}