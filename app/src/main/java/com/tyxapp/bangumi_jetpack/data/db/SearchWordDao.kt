package com.tyxapp.bangumi_jetpack.data.db

import androidx.room.*
import com.tyxapp.bangumi_jetpack.data.SearchWord

@Dao
interface SearchWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchWord: SearchWord): Long

    @Query("SELECT * FROM SEARCH_WORD ORDER BY time DESC")
    suspend fun getSearchWords(): List<SearchWord>

    @Delete
    suspend fun deleteSearchWord(searchWord: SearchWord)
}