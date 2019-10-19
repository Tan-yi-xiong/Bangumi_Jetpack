package com.tyxapp.bangumi_jetpack.data

import androidx.room.*

@Entity(tableName = "SEARCH_WORD", indices = [Index(value = ["word"], unique = true)])
data class SearchWord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var dbId: Int = 0,

    var word: String,
    var time: Long = 0L,

    @Ignore val isFromNet: Boolean
) {
    constructor(): this(0, "", 0L, false)
}