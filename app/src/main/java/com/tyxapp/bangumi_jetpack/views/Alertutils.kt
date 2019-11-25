package com.tyxapp.bangumi_jetpack.views

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar

fun View.snack(msg: String, buttonText: String? = null, listener: View.OnClickListener? = null) {
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).apply {
        if (buttonText != null && listener != null) {
            setAction(buttonText, listener)
        }
        show()
    }
}

fun View.snack(msgRes: Int, buttonText: Int? = null, listener: View.OnClickListener? = null) {
    Snackbar.make(this, msgRes, Snackbar.LENGTH_SHORT).apply {
        if (buttonText != null && listener != null) {
            setAction(buttonText, listener)
        }
        show()
    }
}

fun Activity.alertBuilder(
    title: Int,
    message: Int,
    alertAction: AlertDialog.Builder.() -> AlertDialog.Builder
): AlertDialog.Builder {
    return AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .alertAction()
}

inline fun AlertDialog.Builder.yesButton(
    text: Int = android.R.string.yes,
    crossinline listener: (DialogInterface) -> Unit
): AlertDialog.Builder {
    return setPositiveButton(text) { dialog, _ -> listener(dialog) }
}

inline fun AlertDialog.Builder.noButton(
    text: Int = android.R.string.cancel,
    crossinline listener: (DialogInterface) -> Unit
): AlertDialog.Builder {
    return setNegativeButton(text) { dialog, _ -> listener(dialog) }
}