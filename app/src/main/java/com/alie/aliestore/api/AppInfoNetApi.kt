package com.alie.aliestore.api

import com.alie.aliestore.constant.ConstNet
import com.alie.aliestore.data.NetRspData
import com.alie.aliestore.data.RspAppInfo
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AppInfoNetApi {
    @GET(ConstNet.RELATIVELY_PATH_APP_INFO_LIST)
    suspend fun fetchAppInfoList():NetRspData<List<RspAppInfo>>?

    @GET(ConstNet.RELATIVELY_PATH_APP_INFO)
    suspend fun fetchAppInfoDetail():NetRspData<RspAppInfo>?

    @Streaming
    @GET
    suspend fun downloadApk(@Url url:String?):ResponseBody
}