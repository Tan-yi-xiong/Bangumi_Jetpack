package com.tyxapp.bangumi_jetpack.utilities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tyxapp.bangumi_jetpack.R
import com.tyxapp.bangumi_jetpack.views.alertBuilder
import com.tyxapp.bangumi_jetpack.views.noButton
import com.tyxapp.bangumi_jetpack.views.yesButton
import kotlin.collections.ArrayList

typealias PermissionsCheckCallback = (Boolean) -> Unit
const val PERMISSION_REQUEST_CODE = 7

class PermissionUtil {
    companion object {
        /**
         *
         * @param callback 所有权限已授权回调true, 否则为false
         */
        fun requestPermissions(activity: Activity, premissions: Array<String>, callback: PermissionsCheckCallback) {
            val deniedPermissions = ArrayList<String>()
            premissions.forEach {
                if (ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(it)
                }
            }
            callback(deniedPermissions.isEmpty())
            if (deniedPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, deniedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        }

        fun showSettingPermissionsDialog(activity: Activity) {
            activity.alertBuilder(R.string.text_tips, R.string.text_permission) {
                noButton { it.dismiss() }
                yesButton(R.string.text_get_permission) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                }
            }.show()
        }
    }
}