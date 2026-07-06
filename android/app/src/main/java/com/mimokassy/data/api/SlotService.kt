package com.mimokassy.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SlotService {

    @GET("slots")
    suspend fun listSlots(
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("program_type") programType: List<String>? = null,
        @Query("chef_id") chefId: List<String>? = null,
        @Query("only_available") onlyAvailable: Boolean = false,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SlotListResponse

    @GET("slots/{slotId}")
    suspend fun getSlot(
        @Path("slotId") slotId: String
    ): SlotResponse
}

// ============================================
// DTOs
// ============================================

data class SlotListResponse(
    val items: List<SlotResponse>,
    val meta: Meta
)

data class SlotResponse(
    val id: String,
    val start_at: String,
    val program: ProgramResponse,
    val chef: ChefResponse,
    val total_seats: Int,
    val free_seats: Int,
    val free_rental_kits: Int,
    val price: Int,
    val rental_price: Int,
    val studio_name: String,
    val studio_address: String,
    val studio_lat: Double,
    val studio_lng: Double,
    val status: String
)

data class ProgramResponse(
    val id: String,
    val name: String,
    val description: String?,
    val type: String,
    val duration_min: Int,
    val menu_description: String?
)

data class ChefResponse(
    val id: String,
    val name: String
)

data class Meta(
    val limit: Int,
    val offset: Int,
    val total: Int
)
