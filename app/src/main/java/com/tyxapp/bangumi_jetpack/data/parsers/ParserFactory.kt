package com.tyxapp.bangumi_jetpack.data.parsers

import com.tyxapp.bangumi_jetpack.data.BangumiSource

class ParserFactory {
    companion object{
        fun createHomePageParser(bangumiSource: BangumiSource): IHomePageParser = when(bangumiSource) {
            BangumiSource.Zzzfun -> Zzzfun()
            BangumiSource.DiliDili -> Dilidili()
            else -> throw IllegalAccessException("该资源没有实现IHomePageParser接口")
        }

        fun createSearchParser(bangumiSource: BangumiSource): IsearchParser = when(bangumiSource) {
            BangumiSource.Zzzfun -> Zzzfun()
            BangumiSource.Nico -> Nico()
            BangumiSource.Malimali -> Malimali()
            BangumiSource.Sakura -> Sakura()
            BangumiSource.SiliSili -> Silisili()
            BangumiSource.Qimi -> Qimi()
            else -> throw IllegalAccessException("该资源没有实现IsearchParser接口")
        }

        fun createPlayerVideoParser(bangumiSource: BangumiSource): IPlayerVideoParser = when(bangumiSource) {
            BangumiSource.Zzzfun -> Zzzfun()
            BangumiSource.Nico -> Nico()
            BangumiSource.Malimali -> Malimali()
            BangumiSource.Sakura -> Sakura()
            BangumiSource.SiliSili -> Silisili()
            BangumiSource.Qimi -> Qimi()
            BangumiSource.DiliDili -> Dilidili()
        }
    }
}