package com.mimokassy.data.api

import retrofit2.http.*

interface BookingService {

    @POST("bookings")
    suspend fun createBooking(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: com.mimokassy.data.api.CreateBookingRequest
    ): CreateBookingResponse

    @GET("bookings")
    suspend fun listBookings(
        @Query("status") status: List<String>? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): BookingListResponse

    @GET("bookings/{bookingId}")
    suspend fun getBooking(
        @Path("bookingId") bookingId: String
    ): BookingResponse

    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: String
    ): BookingResponse
}

// ============================================
// DTOs
// ============================================

data class CreateBookingRequest(
    val slot_id: String,
    val seats_count: Int,
    val rental_count: Int,
    val allergy_comment: String?
)

data class CreateBookingResponse(
    val id: String,
    val slot_id: String,
    val client_id: String,
    val seats_count: Int,
    val rental_count: Int,
    val status: String,
    val price_total: Int,
    val allergy_comment: String?,
    val created_at: String,
    val cancelled_at: String?,
    val cancellation_reason: String?,
    val slot: SlotResponse,
    val is_first_booking: Boolean,
    val reminder_hours: List<Int>
)

data class BookingListResponse(
    val items: List<BookingResponse>,
    val meta: Meta
)

data class BookingResponse(
    val id: String,
    val slot_id: String,
    val client_id: String,
    val seats_count: Int,
    val rental_count: Int,
    val status: String,
    val price_total: Int,
    val allergy_comment: String?,
    val created_at: String,
    val cancelled_at: String?,
    val cancellation_reason: String?,
    val slot: SlotResponse
)
