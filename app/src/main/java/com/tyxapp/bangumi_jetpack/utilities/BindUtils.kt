package com.tyxapp.bangumi_jetpack.utilities

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

@BindingAdapter("bindImage")
fun ImageView.bindImage(src: Any) {
    Glide.with(context)
        .load(src)
        .into(this)
}

@BindingAdapter("bindCircleIamge")
fun ImageView.bindCircleIamge(src: Any) {
    Glide.with(context)
        .load(src)
        .transform(CircleCrop())
        .into(this)
}

@BindingAdapter("isGone")
fun bindIsGone(view: View, isGone: Boolean) {
    view.visibility = if (isGone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}