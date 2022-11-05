package com.alie.aliestore.di

import com.alie.aliestore.source.AppInfoDataSource
import com.alie.aliestore.source.AppInfoDataSourceWork
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceWorkBind {
    @Binds
    abstract fun bindAppInfoDataSourceWork(appInfoDataSource: AppInfoDataSource):AppInfoDataSourceWork
}