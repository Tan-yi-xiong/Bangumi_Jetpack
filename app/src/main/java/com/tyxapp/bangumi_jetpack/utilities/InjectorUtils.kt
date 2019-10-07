package com.tyxapp.bangumi_jetpack.utilities

import com.tyxapp.bangumi_jetpack.data.HomeDataRepository
import com.tyxapp.bangumi_jetpack.data.IHomePageParser
import com.tyxapp.bangumi_jetpack.main.viewmodels.CategoryBangumisFactory
import com.tyxapp.bangumi_jetpack.main.viewmodels.CategoryViewModelFactory
import com.tyxapp.bangumi_jetpack.main.viewmodels.NetWorkBangumiViewModelFactory
import com.tyxapp.bangumi_jetpack.main.viewmodels.TimeTableViewModelFactory

object InjectorUtils {
    fun getHomeDataRepository(iHomePageParser: IHomePageParser) = HomeDataRepository(iHomePageParser)

    fun provideNetWorkBangumiViewModelFactory(homeDataRepository: HomeDataRepository) =
            NetWorkBangumiViewModelFactory(homeDataRepository)

    fun provideCategoryViewModelFactory(homeDataRepository: HomeDataRepository) =
            CategoryViewModelFactory(homeDataRepository)

    fun provideTimeTableViewModelFactory(homeDataRepository: HomeDataRepository) =
            TimeTableViewModelFactory(homeDataRepository)

    fun provideCategoryBangumisViewModelFactory(homeDataRepository: HomeDataRepository) =
        CategoryBangumisFactory(homeDataRepository)
}