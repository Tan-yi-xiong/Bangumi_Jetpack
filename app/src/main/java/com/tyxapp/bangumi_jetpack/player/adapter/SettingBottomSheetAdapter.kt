package com.tyxapp.bangumi_jetpack.player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.BangumiApp
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.utilities.LOGI

const val REPLAY_ITEM = R.drawable.ic_replay
const val SPEED_ITEM = R.drawable.ic_speed
const val DOWNLOAD_ITEM = R.drawable.ic_download
const val DANMU_ITEM = R.drawable.ic_danmu

class SettingBottomSheetAdapter(
    private val itemClick: ((Int) -> Unit)?,
    private val currentSpeed: Float
) : RecyclerView.Adapter<SettingBottomSheetAdapter.SettingBottomSheetViewHolder>() {

    private val icons: IntArray = intArrayOf(
        REPLAY_ITEM,
        SPEED_ITEM,
        DOWNLOAD_ITEM,
        DANMU_ITEM
    )
    private val texts = BangumiApp.getContext().resources.getStringArray(R.array.main_bottomsheet_item)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SettingBottomSheetViewHolder {
        return SettingBottomSheetViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.search_helper_item, parent, false
            ),
            itemClick
        )
    }

    override fun getItemCount(): Int = 4

    override fun onBindViewHolder(holder: SettingBottomSheetViewHolder, position: Int) {
        val text = if (icons[position] == SPEED_ITEM) {
            "${texts[position]}  ${currentSpeed}x"
        } else {
            texts[position]
        }
        holder.bind(icons[position], text)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    class SettingBottomSheetViewHolder(
        private val root: View,
        private val itemClick: ((Int) -> Unit)?
    ) : RecyclerView.ViewHolder(root) {
        private val icon = root.findViewById<ImageView>(R.id.imageView)
        private val text = root.findViewById<TextView>(R.id.textView)

        fun bind(iconRes: Int, textRes: String) {
            icon.setImageResource(iconRes)
            text.text = textRes
            root.setOnClickListener { itemClick?.invoke(iconRes) }
        }
    }

}

