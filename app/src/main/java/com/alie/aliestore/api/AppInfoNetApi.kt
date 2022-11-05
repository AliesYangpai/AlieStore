package com.alie.aliestore.api

import com.alie.aliestore.data.AppInfo
import com.alie.aliestore.data.NetRspData
import com.alie.aliestore.data.RspAppInfo

interface AppInfoNetApi {
    suspend fun fetchAppInfoList():NetRspData<List<RspAppInfo>>?
    suspend fun fetchAppInfoDetail():NetRspData<RspAppInfo>?
}