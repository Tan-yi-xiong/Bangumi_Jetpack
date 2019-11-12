package com.tyxapp.bangumi_jetpack.data

/**
 * 番剧数据类, 主页使用
 *
 */
open class Bangumi(
    var id: String,
    val source: BangumiSource,
    val name: String = "",
    val cover: String = "",//封面
    val jiTotal: String = ""//集数
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Bangumi) {
            id == other.id && source == other.source
                    && name == other.name && cover == other.cover && jiTotal == other.jiTotal
        } else {
            false
        }
    }
}