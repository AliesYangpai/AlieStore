package com.alie.aliestore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alie.aliestore.data.AppInfo
import com.alie.aliestore.data.UiState
import com.alie.aliestore.repo.AppInfoRepository
import com.alie.aliestore.ui.data.TipUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel(private val appInfoRepository: AppInfoRepository) : ViewModel() {


    private val _tipUiState = MutableStateFlow(TipUiState())
    val tipUiState: StateFlow<UiState<AppInfo>> = _tipUiState


    init {
        fetchAppDetail()
    }

    fun fetchAppDetail(coroutineScope: CoroutineScope = viewModelScope) {
        coroutineScope.launch {
            appInfoRepository.fetchAppInfoDetail().map {
                when {
                    it.ret -> when(it.apiData) {
                        null-> TipUiState(isSuccess = false, msg = it.msg)
                        else-> when(val targetData = it.apiData.data) {
                            null-> TipUiState(isSuccess = false, msg = it.msg)
                            else-> TipUiState(isSuccess = true, data = AppInfo(name = targetData.name, detail = targetData.detail))
                        }
                    }
                    else -> TipUiState(isSuccess = false, msg = it.msg)
                }
            }.collectLatest {
                _tipUiState.value = it
            }
        }
    }
}