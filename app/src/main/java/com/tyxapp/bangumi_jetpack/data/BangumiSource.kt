package com.tyxapp.bangumi_jetpack.data

enum class BangumiSource {
    Nico, Zzzfun, Sakura, DiliDili, SiliSili, Qimi, Malimali,
}

/**
 * 将某些网站的网站名转换成中文名
 *
 */
class BangumiSourceNameConversion {
    companion object {
        fun getConversionName(bangumiSource: BangumiSource): String = when (bangumiSource) {
            BangumiSource.Malimali -> "嘛哩嘛哩"

            BangumiSource.Sakura -> "樱花动漫"

            else -> bangumiSource.name
        }
    }
}