package com.tyxapp.bangumi_jetpack.player.danmakuparser

import android.graphics.Color
import com.tyxapp.bangumi_jetpack.utilities.LOGI
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.android.AndroidFileSource
import master.flame.danmaku.danmaku.util.IOUtils
import org.json.JSONObject

class ZzzFunDanmakuParser : BaseDanmakuParser() {
    override fun parse(): IDanmakus? {
        if (mDataSource != null) {
            val androidFileSource = mDataSource as AndroidFileSource
            val danmakus = Danmakus()

            val json = IOUtils.getString(androidFileSource.data())
            val danmus = JSONObject(json).takeIf { !it.isNull("data") }
                ?.getJSONArray("data") ?: return null


            for (i in 0 until danmus.length()) {
                //[ "6.21", "1", "#39ccff", "用户zzzfun", "蓝色不错" ]

                val danmu = danmus.getJSONArray(i)
                val time = parseFloat(danmu.getString(0)) * 1000
                val type = parseInteger(danmu.getString(1))
                val color = Color.parseColor(danmu.getString(2))
                val text = danmu.getString(4)

                val danmaku = mContext.mDanmakuFactory.createDanmaku(1, mContext).apply {
                    this.text = text
                    textColor = color
                    this.time = time.toLong()
                    textSize = 25.0f * (mDispDensity - 0.6f)
                    flags = mContext.mGlobalFlagValues
                    timer = mTimer
                }
                danmakus.addItem(danmaku)
            }
            return danmakus
        }
        return null
    }

    private fun parseFloat(floatStr: String): Float {
        return try {
            floatStr.toFloat()
        } catch (e: NumberFormatException) {
            0.0f
        }
    }

    private fun parseInteger(intStr: String): Int {
        return try {
            intStr.toInt()
        } catch (e: NumberFormatException) {
            0
        }

    }

}