package com.tyxapp.bangumi_jetpack.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 番剧数据类, 主页使用
 *
 */
open class Bangumi (
    var id: String,
    val source: BangumiSource,
    val name: String = "",
    val cover: String = "",//封面
    val jiTotal: String = ""//集数
): Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeString(id)
            writeString(source.name)
            writeString(name)
            writeString(cover)
            writeString(jiTotal)
        }
    }

    override fun describeContents(): Int = 0

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        BangumiSource.valueOf(parcel.readString()),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )


    override fun equals(other: Any?): Boolean {
        return if (other is Bangumi) {
            id == other.id && source == other.source
                    && name == other.name && cover == other.cover && jiTotal == other.jiTotal
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return id.hashCode() + name.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<Bangumi> {
        override fun createFromParcel(parcel: Parcel): Bangumi {
            return Bangumi(parcel)
        }

        override fun newArray(size: Int): Array<Bangumi?> {
            return arrayOfNulls(size)
        }
    }
}