package com.nelayanku.apps.mapsApi

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MapInstance {

    @GET("/v1/revgeocode")
    suspend fun getLocation(
        @Query("at") at: String,
        @Query("apiKey") apiKey: String = MapConstant.apiKey
    ): Places

    companion object {
        fun create(): MapInstance {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(MapConstant.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(MapInstance::class.java)
        }
    }
}