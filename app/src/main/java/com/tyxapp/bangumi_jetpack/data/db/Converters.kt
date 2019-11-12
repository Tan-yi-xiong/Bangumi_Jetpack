package com.tyxapp.bangumi_jetpack.data.db

import androidx.room.TypeConverter
import com.tyxapp.bangumi_jetpack.data.BangumiSource
import com.tyxapp.bangumi_jetpack.data.DownLoadState

class Converters {
    @TypeConverter fun sourceToString(bangumiSource: BangumiSource) : String = bangumiSource.name

    @TypeConverter fun stringToSource(string: String) : BangumiSource = BangumiSource.valueOf(string)

    @TypeConverter fun downloadStateToInt(downLoadState: DownLoadState) : String = downLoadState.name

    @TypeConverter fun stringTodownloadState(string: String) : DownLoadState = DownLoadState.valueOf(string)
}