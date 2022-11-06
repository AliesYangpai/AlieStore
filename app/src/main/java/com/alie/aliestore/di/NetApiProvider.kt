package com.alie.aliestore.di

import android.util.Log
import com.alie.aliestore.api.AppInfoNetApi
import com.alie.aliestore.constant.ConstNet
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetApiProvider {

    @Provides
    fun provideRetrofit():Retrofit = Retrofit.Builder()
        .baseUrl(ConstNet.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor {
                Log.d("HttpLoggingInterceptor",it)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build())
        .build()


    @Provides
    fun provideAppInfoNetApi(retrofit: Retrofit): AppInfoNetApi = retrofit.create(AppInfoNetApi::class.java)

}