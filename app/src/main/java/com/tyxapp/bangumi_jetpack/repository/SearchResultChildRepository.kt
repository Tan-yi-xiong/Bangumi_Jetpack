package com.tyxapp.bangumi_jetpack.repository

import com.tyxapp.bangumi_jetpack.data.Bangumi
import com.tyxapp.bangumi_jetpack.data.Listing
import com.tyxapp.bangumi_jetpack.data.parsers.IsearchParser

class SearchResultChildRepository(
    private val searchParse: IsearchParser
) {
    fun getSearchResult(searchWord: String): Listing<Bangumi> =
        searchParse.getSearchResult(searchWord)
}