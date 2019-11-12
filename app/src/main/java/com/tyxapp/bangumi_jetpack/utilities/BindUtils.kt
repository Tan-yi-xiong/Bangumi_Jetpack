package com.tyxapp.bangumi_jetpack.utilities

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.tyxapp.bangumi_jetpack.R

@BindingAdapter("bindImage")
fun ImageView.bindImage(src: Any?) {
    Glide.with(context)
        .load(src)
        .apply(glideOptions)
        .into(this)
}

@BindingAdapter("bindCircleIamge")
fun ImageView.bindCircleIamge(src: Any) {
    Glide.with(context)
        .load(src)
        .apply(glideOptions)
        .transform(CircleCrop())
        .into(this)
}

@BindingAdapter("imageUrl", "angle")
fun ImageView.bindRoundedCorners(src: Any?, angle: Int) {
    Glide.with(context)
        .load(src)
        .apply(glideOptions)
        .transform(CenterCrop(), RoundedCorners(angle))
        .into(this)
}

@BindingAdapter("isGone")
fun View.isGone(isGone: Boolean) {
    this.visibility = if (isGone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("isSelect", "selectText", "unSelectText")
fun Button.bindIsSelect(isSelect: Boolean, selectText: String, unSelectText: String) {
    this.isSelected = isSelect
    text = if (isSelect) selectText else unSelectText
}

@BindingAdapter("isSelect")
fun View.isSelect(isSelect: Boolean) {
    this.isSelected = isSelect
}

val glideOptions = RequestOptions()
    .error(R.drawable.error)
