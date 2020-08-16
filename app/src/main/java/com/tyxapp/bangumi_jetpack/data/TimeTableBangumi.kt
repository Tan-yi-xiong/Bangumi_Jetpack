package com.tyxapp.bangumi_jetpack.data

import androidx.recyclerview.widget.DiffUtil

data class TimeTableBangumi (
    var id: String,
    val source: BangumiSource,
    val name: String,
    val cover: String,
    val newJi: String,
    val isUpdate: Boolean = false
) {

    companion object {
        val ItemCallback = object : DiffUtil.ItemCallback<TimeTableBangumi>() {
            override fun areItemsTheSame(
                oldItem: TimeTableBangumi,
                newItem: TimeTableBangumi
            ): Boolean {
                return newItem == oldItem
            }

            override fun areContentsTheSame(
                oldItem: TimeTableBangumi,
                newItem: TimeTableBangumi
            ): Boolean {
                return newItem == oldItem
            }
        }
    }

}