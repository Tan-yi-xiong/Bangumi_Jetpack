package com.tyxapp.bangumi_jetpack.data.parsers

import com.tyxapp.bangumi_jetpack.data.BangumiSource
import java.lang.IllegalArgumentException

/**
 * 创建解析类工厂
 *
 */
class ParserFactory {
    companion object{
        fun createHomePageParser(bangumiSource: BangumiSource): IHomePageParser = when(bangumiSource) {
            BangumiSource.Zzzfun -> Zzzfun()
            BangumiSource.DiliDili -> Dilidili()
            BangumiSource.BimiBimi -> BimiBimi()
            else -> throwException(bangumiSource)
        }

        fun createSearchParser(bangumiSource: BangumiSource): IsearchParser = when(bangumiSource) {
            BangumiSource.Zzzfun -> Zzzfun()
            BangumiSource.Nico -> Nico()
            BangumiSource.Malimali -> Malimali()
            BangumiSource.DiliDili -> Dilidili()
            BangumiSource.Sakura -> Sakura()
            BangumiSource.SiliSili -> Silisili()
            BangumiSource.Qimi -> Qimi()
            BangumiSource.BimiBimi -> BimiBimi()
        }

        fun createPlayerVideoParser(bangumiSource: BangumiSource): IPlayerVideoParser = when(bangumiSource) {
            BangumiSource.Zzzfun -> Zzzfun()
            BangumiSource.Nico -> Nico()
            BangumiSource.Malimali -> Malimali()
            BangumiSource.Sakura -> Sakura()
            BangumiSource.SiliSili -> Silisili()
            BangumiSource.Qimi -> Qimi()
            BangumiSource.DiliDili -> Dilidili()
            BangumiSource.BimiBimi -> BimiBimi()
        }

        private fun throwException(bangumiSource: BangumiSource): Nothing {
            throw IllegalArgumentException("$bangumiSource 没有找到此解析器")
        }
    }
}