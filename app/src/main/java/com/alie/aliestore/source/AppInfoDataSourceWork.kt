package com.alie.aliestore.source

import com.alie.aliestore.api.AppInfoNetApi
import com.alie.aliestore.data.ApiData
import com.alie.aliestore.data.NetRspData
import com.alie.aliestore.data.RspAppInfo

interface AppInfoDataSourceWork:AppInfoNetApi{
    suspend fun fetchLocalAppInfoList(): ApiData<List<RspAppInfo>>
    suspend fun fetchLocalAppInfoDetail(): ApiData<RspAppInfo>
}