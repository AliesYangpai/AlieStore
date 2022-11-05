package com.alie.aliestore.source

import com.alie.aliestore.api.AppInfoNetApi
import com.alie.aliestore.data.*
import javax.inject.Inject

class AppInfoDataSource @Inject constructor(private val appInfoNetApi: AppInfoNetApi) : AppInfoDataSourceWork {
    override suspend fun fetchLocalAppInfoList(): ApiData<List<RspAppInfo>> {
        throw java.lang.Exception()
    }

    override suspend fun fetchLocalAppInfoDetail(): ApiData<RspAppInfo> {
        throw java.lang.Exception()
    }

    override suspend fun fetchAppInfoList(): NetRspData<List<RspAppInfo>>? =
        appInfoNetApi.fetchAppInfoList()

    override suspend fun fetchAppInfoDetail(): NetRspData<RspAppInfo>? =
        appInfoNetApi.fetchAppInfoDetail()
}