package com.alie.aliestore.test

import com.alie.aliestore.test.data.AppInfoData
import com.alie.aliestore.test.data.Banner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class TestDataSource @Inject constructor(){

    suspend fun fetchToken() = "name"

    suspend fun fetchBannerList() = flow<List<Banner>> {
        delay(1000)
        emit(listOf(Banner("ad1"),Banner("ad2"), Banner("ad3")))

    }

    suspend fun fetchAppList() = flow<List<AppInfoData>> {
        delay(1000)
        emit(listOf(AppInfoData("app1"),AppInfoData("app2"), AppInfoData("app3")))
    }
}