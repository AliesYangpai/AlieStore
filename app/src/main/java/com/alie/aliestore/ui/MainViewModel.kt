package com.alie.aliestore.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alie.aliestore.data.AppInfo
import com.alie.aliestore.data.UiState
import com.alie.aliestore.repo.AppInfoRepository
import com.alie.aliestore.ui.data.TipUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val appInfoRepository: AppInfoRepository) :
    ViewModel() {


    private val _tipUiState = MutableStateFlow(TipUiState())
    val tipUiState: StateFlow<UiState<AppInfo>> = _tipUiState


    init {
        fetchAppDetail()
    }

    fun fetchAppDetail(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            appInfoRepository.fetchAppInfoDetail().map {
                when {
                    it.ret -> when (it.apiData) {
                        null -> TipUiState(isSuccess = false, msg = it.msg)
                        else -> when (val targetData = it.apiData.data) {
                            null -> TipUiState(isSuccess = false, msg = it.msg)
                            else -> TipUiState(
                                isSuccess = true,
                                data = AppInfo(
                                    name = targetData.name,
                                    describe = targetData.describe,
                                    avatar = targetData.avatar
                                )
                            )
                        }
                    }
                    else -> TipUiState(isSuccess = false, msg = it.msg)
                }
            }.collectLatest {
                _tipUiState.value = it
            }
        }
    }


    fun downloadApk(url: String, coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            appInfoRepository.downloadApk(url).collect {
                when (it.ret) {
                    true -> Log.d(
                        "MainViewModel",
                        "downloadApk currentSize:${it.apiData?.currentSize} totalSize:${it.apiData?.totalSize}"
                    )
                    else -> Log.d(
                        "MainViewModel",
                        "downloadApk error:${it.msg}")
                }
            }
        }
    }

    fun downloadApkInRange(url: String, coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            appInfoRepository.downloadApkInRange(url).collect {
//                when (it.ret) {
//                    true -> Log.d(
//                        "MainViewModel",
//                        "downloadApk currentSize:${it.apiData?.currentSize} totalSize:${it.apiData?.totalSize}"
//                    )
//                    else -> Log.d(
//                        "MainViewModel",
//                        "downloadApk error:${it.msg}")
//                }
            }
        }
    }

}