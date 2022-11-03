package com.alie.aliestore.test

import com.alie.aliestore.test.data.AppInfoData
import com.alie.aliestore.test.data.Banner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class TestRepo @Inject constructor(private val testDataSource: TestDataSource) {


    suspend fun fetchToken() = flow {
        emit(testDataSource.fetchToken())
    }.catch {
        emit(it.message ?: "")
    }

    suspend fun fetchBannerList() :Flow<List<Banner>> = testDataSource.fetchBannerList()

    suspend fun fetchAppList() :Flow<List<AppInfoData>> = testDataSource.fetchAppList()
}