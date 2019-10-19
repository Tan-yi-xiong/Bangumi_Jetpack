package com.tyxapp.bangumi_jetpack.data

import com.tyxapp.bangumi_jetpack.data.parsers.IsearchParse

class SearchResultChildRepository(
    private val searchParse: IsearchParse
) {
    fun getSearchResult(searchWord: String): Listing<Bangumi> =
        searchParse.getSearchResult(searchWord)
}