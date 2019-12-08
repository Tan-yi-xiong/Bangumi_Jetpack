package com.tyxapp.bangumi_jetpack.player.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.tyxapp.bangumi_jetpack.player.adapter.SettingBottomSheetAdapter


class SettingBottomSheet : BaseBottomSheet() {
    private var mItemClick: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = super.onCreateView(inflater, container, savedInstanceState) as RecyclerView
        recyclerView.background = requireActivity().window.decorView.background
        return recyclerView.apply {
            adapter = SettingBottomSheetAdapter(mItemClick, arguments!!.getFloat(SPEED_ARG))
        }
    }

    fun setItemClickListener(itemClick: (Int) -> Unit) {
        mItemClick = itemClick
    }

    companion object{
        private const val SPEED_ARG = "SPEED_ARG"
        fun newInstance(currentSpeed: Float): SettingBottomSheet {
            return SettingBottomSheet().apply {
                arguments = bundleOf(SPEED_ARG to currentSpeed)
            }
        }
    }
}