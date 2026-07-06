package com.mimokassy.data.models

import java.util.Date

enum class SlotStatus {
    SCHEDULED, CANCELLED
}

data class Slot(
    val id: String,
    val startAt: Date,
    val program: Program,
    val chef: Chef,
    val totalSeats: Int,
    val freeSeats: Int,
    val freeRentalKits: Int,
    val price: Int,
    val rentalPrice: Int,
    val studioName: String,
    val studioAddress: String,
    val studioLat: Double,
    val studioLng: Double,
    val status: SlotStatus
) {
    val isAvailable: Boolean
        get() = freeSeats > 0 && status == SlotStatus.SCHEDULED

    val maxSeatsToBook: Int
        get() = minOf(freeSeats, program.capacityCap, 3)

    val isFull: Boolean
        get() = freeSeats == 0

    val isCancelled: Boolean
        get() = status == SlotStatus.CANCELLED

    val displayDateTime: String
        get() = android.text.format.DateFormat.format("EEE, d MMM · HH:mm", startAt).toString()
}
