package com.tyxapp.bangumi_jetpack.utilities

import com.tyxapp.bangumi_jetpack.data.db.AppDataBase
import com.tyxapp.bangumi_jetpack.data.parsers.IHomePageParser
import com.tyxapp.bangumi_jetpack.data.parsers.IPlayerVideoParser
import com.tyxapp.bangumi_jetpack.data.parsers.IsearchParser
import com.tyxapp.bangumi_jetpack.data.parsers.SearchWordParser
import com.tyxapp.bangumi_jetpack.main.history.HistoryViewModelFactory
import com.tyxapp.bangumi_jetpack.main.home.viewmodels.*
import com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels.DownloadDetailViewModelFactory
import com.tyxapp.bangumi_jetpack.main.mydownload.viewmodels.MyDownloadViewModelFactory
import com.tyxapp.bangumi_jetpack.main.search.viewmodels.SearchHelperViewModelFactory
import com.tyxapp.bangumi_jetpack.main.search.viewmodels.SearchResultChildViewModelFactory
import com.tyxapp.bangumi_jetpack.player.PlayerViewModelFactory
import com.tyxapp.bangumi_jetpack.repository.*

object InjectorUtils {

    fun getHomeDataRepository(iHomePageParser: IHomePageParser) =
        HomeDataRepository(iHomePageParser)

    private fun getMyDownloadRepository(): MyDownloadRepository {
        return MyDownloadRepository.getInstance()
    }

    private fun getSearchHelperRepository() =
        SearchHelperRepository(AppDataBase.getInstance().searchDao(), SearchWordParser.getInstance())

    private fun getSearchResultChildRepository(searchParse: IsearchParser) =
        SearchResultChildRepository(searchParse)

    fun provideNetWorkBangumiViewModelFactory(homeDataRepository: HomeDataRepository) =
        NetWorkBangumiViewModelFactory(
            homeDataRepository
        )

    fun provideCategoryViewModelFactory(homeDataRepository: HomeDataRepository) =
        CategoryViewModelFactory(homeDataRepository)

    fun provideTimeTableViewModelFactory(homeDataRepository: HomeDataRepository) =
        TimeTableViewModelFactory(homeDataRepository)

    fun provideCategoryBangumisViewModelFactory(homeDataRepository: HomeDataRepository) =
        CategoryBangumisFactory(homeDataRepository)

    fun provideSearchHelperViewModelFactory() =
        SearchHelperViewModelFactory(
            getSearchHelperRepository()
        )

    fun provideDilidiliUpdateBangumiViewModelFactory(homeDataRepository: HomeDataRepository) =
        DilidiliUpdateBangumiViewModelFactory(homeDataRepository)

    fun provideSearchResultChildViewModelFactory(searchParse: IsearchParser): SearchResultChildViewModelFactory {
        return SearchResultChildViewModelFactory(
            getSearchResultChildRepository(searchParse)
        )
    }

    fun providePlayerViewModelFactory(playerVideoParser: IPlayerVideoParser) =
        PlayerViewModelFactory(PlayerRepository(
            playerVideoParser,
            AppDataBase.getInstance().bangumiDetailDao(),
            AppDataBase.getInstance().videoRecordDao())
        )

    fun provideBangumiFollowViewModelFactory() =
        BangumiFollowViewModelFactory(
            BangumiFollowRepository.getInstance()
        )

    fun provideHistoryViewModelFactory() =
        HistoryViewModelFactory(HistoryWitchRepository.getInstance())

    fun provideMyDownloadViewModelFactory() =
        MyDownloadViewModelFactory(
            getMyDownloadRepository()
        )

    fun provideDownloadDetailViewModelFactory() =
        DownloadDetailViewModelFactory(
            DownloadDetailRepository.getInstance()
        )
}