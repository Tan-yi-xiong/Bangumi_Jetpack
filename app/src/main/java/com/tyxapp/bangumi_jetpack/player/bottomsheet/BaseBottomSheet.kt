package com.tyxapp.bangumi_jetpack.player.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RecyclerView(requireContext())
    }

    override fun onStart() {
        super.onStart()

        //不折叠bottomSheet
        val bottomSheetDialog = dialog as BottomSheetDialog
        val frameLayout =
            bottomSheetDialog.delegate.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

        BottomSheetBehavior.from(frameLayout).state = BottomSheetBehavior.STATE_EXPANDED
    }
}