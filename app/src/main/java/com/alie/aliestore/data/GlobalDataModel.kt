package com.alie.aliestore.data

/**
 * 全局数据模型定义，这里是通用的全局数据模型
 * 分为以下几种
 * Api返回模型
 * DataSource返回模型
 * UiState返回模型
 */


/**
 * api 通用数据模型
 *
 * @param T
 * @property code
 * @property msg
 * @property apiData
 */
open class ApiData<T>(
    open val code: Int = 0,
    open val msg: String = "",
    open val data: T? = null
)

open class NetRspData<T>(
    code: Int = 0,
    msg: String = "",
    data: T? = null
) :
    ApiData<T>(code, msg, data)

/**
 * DataSource通用数据模型
 *
 * @param T
 * @property ret
 * @property msg
 * @property apiData
 */
data class SourceData<T>(
    val ret: Boolean = true,
    val msg: String = "",
    val apiData: T? = null
)


/**
 * uiState 类型
 *
 * @param T
 * @property isSuccess
 * @property msg
 * @property data
 * @property isVisible
 * @property isEnable
 */
open class UiState<T>(
    isVisible: Boolean = true,
    isEnable: Boolean = true,
    open val isSuccess: Boolean = true,
    open val msg: String = "",
    open val data: T? = null,
) : UiViewState(isVisible, isEnable)

/**
 * view控件状态
 *
 * @property isVisible
 * @property isEnable
 */
open class UiViewState(
    open val isVisible: Boolean = true,
    open val isEnable: Boolean = true
)