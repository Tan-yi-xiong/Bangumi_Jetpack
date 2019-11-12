package com.tyxapp.bangumi_jetpack.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "SEARCH_WORD", indices = [Index(value = ["word"], unique = true)])
data class SearchWord(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var word: String,
    var time: Long = 0L,

    @Ignore
    val isFromNet: Boolean
) {
    constructor(): this(0, "", 0L, false)
}