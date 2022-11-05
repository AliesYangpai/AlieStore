package com.alie.aliestore.ui.data

import com.alie.aliestore.data.AppInfo
import com.alie.aliestore.data.UiState

 class TipUiState(
    isVisible: Boolean = true,
    isEnable: Boolean = true,
    isSuccess: Boolean = true,
    msg: String = "",
    data: AppInfo? = null
) : UiState<AppInfo>(isVisible, isEnable, isSuccess, msg, data)