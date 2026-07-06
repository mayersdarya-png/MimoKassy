package com.mimokassy.data.api

import retrofit2.http.GET

interface ChefService {

    @GET("chefs")
    suspend fun listChefs(): ChefListResponse
}

data class ChefListResponse(
    val items: List<ChefResponse>,
    val meta: Meta
)
