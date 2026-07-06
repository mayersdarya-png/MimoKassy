package com.mimokassy.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Для эмулятора Android — localhost = 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Для реального устройства — IP твоего компьютера
    // private const val BASE_URL = "http://192.168.1.xxx:8000/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
