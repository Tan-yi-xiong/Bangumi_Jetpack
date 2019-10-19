package com.tyxapp.bangumi_jetpack.utilities

import com.tyxapp.bangumi_jetpack.data.HomeDataRepository
import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.parsers.IHomePageParser
import com.tyxapp.bangumi_jetpack.data.SearchHelperRepository
import com.tyxapp.bangumi_jetpack.data.SearchResultChildRepository
import com.tyxapp.bangumi_jetpack.data.parsers.IsearchParse
import com.tyxapp.bangumi_jetpack.main.viewmodels.*

object InjectorUtils {
    fun getHomeDataRepository(iHomePageParser: IHomePageParser) = HomeDataRepository(iHomePageParser)

    private fun getSearchHelperRepository() =
        SearchHelperRepository(AppDataBase.getInstance().searchDao())

    private fun getSearchResultChildRepository(searchParse: IsearchParse) =
        SearchResultChildRepository(searchParse)

    fun provideNetWorkBangumiViewModelFactory(homeDataRepository: HomeDataRepository) =
            NetWorkBangumiViewModelFactory(homeDataRepository)

    fun provideCategoryViewModelFactory(homeDataRepository: HomeDataRepository) =
            CategoryViewModelFactory(homeDataRepository)

    fun provideTimeTableViewModelFactory(homeDataRepository: HomeDataRepository) =
            TimeTableViewModelFactory(homeDataRepository)

    fun provideCategoryBangumisViewModelFactory(homeDataRepository: HomeDataRepository) =
        CategoryBangumisFactory(homeDataRepository)

    fun provideSearchHelperViewModelFactory() = SearchHelperViewModelFactory(
        getSearchHelperRepository())

    fun provideSearchResultChildRepository(searchParse: IsearchParse): SearchResultChildViewModelFactory {
        return SearchResultChildViewModelFactory(getSearchResultChildRepository(searchParse))
    }
}