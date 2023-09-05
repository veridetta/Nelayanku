package com.nelayanku.apps.mapsApi

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SearcMapInstance {
    @GET("/v1/geocode")
    suspend fun getSearch(
        @Query("q") at: String,
        @Query("apiKey") apiKey: String = MapConstant.apiKey
    ): Places

    companion object {
        fun create(): SearcMapInstance {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(MapConstant.urlSearc)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(SearcMapInstance::class.java)
        }
    }
}