package com.alie.aliestore.repo

import com.alie.aliestore.data.SourceData
import com.alie.aliestore.source.AppInfoDataSourceWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * repo中获取 sourceData 并将数据转化为 核心uiData
 *
 * @property appInfoDataSourceWork
 */

class AppInfoRepository @Inject constructor(private val appInfoDataSourceWork: AppInfoDataSourceWork) {

    suspend fun fetchAppInfoList() = flow {
        emit(
            when (val fetchData = appInfoDataSourceWork.fetchAppInfoList()) {
                null -> SourceData(ret = false, msg = "no data")
                else -> SourceData(apiData = fetchData)
            }
        )
    }.catch {
        emit(SourceData(ret = false, msg = it.message ?: "unknown error"))
    }.flowOn(Dispatchers.IO)

    suspend fun fetchAppInfoDetail() = flow {
        val data = when (val fetchData = appInfoDataSourceWork.fetchAppInfoDetail()) {
            null -> SourceData(ret = false, msg = "no data")
            else -> SourceData(apiData = fetchData)
        }
        emit(data)
    }.catch {
        emit(SourceData(ret = false, msg = it.message ?: "unknown error"))
    }.flowOn(Dispatchers.IO)
}