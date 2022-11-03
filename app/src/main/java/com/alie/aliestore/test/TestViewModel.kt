package com.alie.aliestore.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(private val testRepo: TestRepo) : ViewModel() {



    init {
        viewModelScope.launch {
            testRepo.fetchToken().collectLatest {
                println("data:$it")
            }
        }
    }

     fun syncData() {
         viewModelScope.launch {
             val flowBanner = testRepo.fetchBannerList()
             val flowAppList = testRepo.fetchAppList()
             flowBanner.combine(flowAppList) { banners, appList ->
                 banners.forEach {
                     println("syncData banners it:${it.name}")
                 }

                 appList.forEach {
                     println("syncData applist it:${it.name}")
                 }
             }.catch {
                 println("syncData error ${it.message}")
             }.collect()
         }

    }
}