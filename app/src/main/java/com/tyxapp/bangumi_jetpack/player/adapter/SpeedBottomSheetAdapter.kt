package com.tyxapp.bangumi_jetpack.player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.utilities.isGone

const val SPEED_1 = 0.5F
const val SPEED_2 = 0.75F
const val SPEED_3 = 1.0F
const val SPEED_4 = 1.5F
const val SPEED_5 = 2.0F

class SpeedBottomSheetAdapter(
    private val itemClickListener: ((Float) -> Unit)?
) : RecyclerView.Adapter<SpeedBottomSheetAdapter.ViewHolder>() {
    private val speeds = floatArrayOf(
        SPEED_1,
        SPEED_2,
        SPEED_3,
        SPEED_4,
        SPEED_5
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.search_helper_item,
                parent, false
            ),
            itemClickListener
        )
    }

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(speeds[position])
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }


    class ViewHolder(
        private val root: View,
        private val itemClickListener: ((Float) -> Unit)?
    ) : RecyclerView.ViewHolder(root) {
        private val icon = root.findViewById<ImageView>(R.id.imageView)
        private val speedText = root.findViewById<TextView>(R.id.textView)

        init {
            icon.isGone(true)
        }

        fun bind(speed: Float) {
            speedText.text = "${speed}x"
            root.setOnClickListener { itemClickListener?.invoke(speed) }
        }
    }
}