package com.mimokassy.data.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val sharedPref = context.getSharedPreferences("mimo_kassy", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        val newRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private lateinit var authInterceptor: AuthInterceptor

    fun init(context: Context) {
        authInterceptor = AuthInterceptor(context)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val slotApi: SlotService by lazy { retrofit.create(SlotService::class.java) }
    val bookingApi: BookingService by lazy { retrofit.create(BookingService::class.java) }
    val chefApi: ChefService by lazy { retrofit.create(ChefService::class.java) }
}
