package com.tyxapp.bangumi_jetpack.utilities

import android.text.TextUtils
import okhttp3.internal.and
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MD5Util {
    companion object {
        fun md5(str: String): String {
            if (TextUtils.isEmpty(str)) {
                return ""
            }
            try {
                var str2 = ""
                for (b in MessageDigest.getInstance("MD5").digest(str.toByteArray())) {
                    var hexString = Integer.toHexString(b and 255)
                    if (hexString.length == 1) {
                        hexString = "0$hexString"
                    }
                    str2 = str2 + hexString
                }
                return str2
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                return ""
            }


        }
    }
}