package com.arsample.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {
    private const val BASE_URL = "http://165.227.200.61/api/"

    fun <T> buildService(service: Class<T> ): T{
        val client = OkHttpClient
            .Builder()
            .build()

        val retrofit =  Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(service)
    }
}