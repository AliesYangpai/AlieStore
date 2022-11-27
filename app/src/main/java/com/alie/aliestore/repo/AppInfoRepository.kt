package com.alie.aliestore.repo

import android.content.Context
import android.os.Environment
import android.util.Log
import com.alie.aliestore.data.DownloadInfo
import com.alie.aliestore.data.SourceData
import com.alie.aliestore.source.AppInfoDataSourceWork
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.nio.Buffer
import javax.inject.Inject

/**
 * repo中获取 sourceData 并将数据转化为 核心uiData
 *
 * @property appInfoDataSourceWork
 */

class AppInfoRepository @Inject constructor(
    private val appInfoDataSourceWork: AppInfoDataSourceWork,
    @ApplicationContext private val ctx: Context
) {

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


    suspend fun downloadApk(url: String) = flow {
        val response = appInfoDataSourceWork.downloadApk(url)
        val fileDir = File(getApkRootDir())
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val fileApk = File(fileDir.canonicalPath, "MjWeather.apk")
        if (fileApk.exists()) {
            fileApk.delete()
        }

        val bufferRead = ByteArray(1024 * 4  )
        val totalLength = response.contentLength()
        var currentLength = 0
        response.byteStream().use { inputStream ->
            FileOutputStream(fileApk).use { fos ->
                var readPerLength = -1
                while (inputStream.read(bufferRead).also {
                        readPerLength = it
                    } != -1) {
                    fos.write(bufferRead,0,readPerLength)
                    currentLength+=readPerLength
                    emit(SourceData(ret = true, apiData = DownloadInfo(currentLength.toLong(),totalLength)))
                }
                fos.flush()
            }
        }
        emit(SourceData(ret = true, apiData = DownloadInfo(currentLength.toLong(),9999)))
    }.catch {
        emit(SourceData(ret = false, it.message ?: "unknown error"))
    }.flowOn(Dispatchers.IO)

    private fun getApkRootDir() =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "downloadApks"


    private fun checkToCreateDir(path: String) {
        val fileDir = File(path)
        if (!fileDir.exists()) fileDir.mkdir()
    }
}