package com.tyxapp.bangumi_jetpack.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun SourceToString(bangumiSource: BangumiSource) : String = bangumiSource.name

    @TypeConverter fun StringToSource(string: String) : BangumiSource = BangumiSource.valueOf(string)
}