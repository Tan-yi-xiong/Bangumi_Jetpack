package com.tyxapp.bangumi_jetpack.player.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tyxapp.bangumi_jetpack.player.adapter.SpeedBottomSheetAdapter

class SpeedBottomSheet: BaseBottomSheet() {
    var itemClickListener: ((Float) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = super.onCreateView(inflater, container, savedInstanceState) as RecyclerView
        return recyclerView.apply {
            adapter = SpeedBottomSheetAdapter(itemClickListener)
        }
    }


    companion object{
        fun newInstence() = SpeedBottomSheet()
    }
}