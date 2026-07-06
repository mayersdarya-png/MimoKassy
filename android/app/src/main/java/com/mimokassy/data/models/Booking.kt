package com.mimokassy.data.models

import java.util.Date

enum class BookingStatus {
    ACTIVE, CANCELLED, LATE_CANCEL, STUDIO_CANCELLED
}

data class Booking(
    val id: String,
    val slotId: String,
    val clientId: String,
    val seatsCount: Int,
    val rentalCount: Int,
    val status: BookingStatus,
    val priceTotal: Int,
    val allergyComment: String?,
    val createdAt: String,
    val cancelledAt: String?,
    val cancellationReason: String?,
    val slot: Slot?
) {
    val isUpcoming: Boolean
        get() = status == BookingStatus.ACTIVE && slot?.startAt?.after(Date()) == true

    val isPast: Boolean
        get() = slot?.startAt?.before(Date()) == true

    val ownSeatsCount: Int
        get() = seatsCount - rentalCount

    val displayStatus: String
        get() = if (!status.isActive() && slot?.startAt?.before(Date()) == true) {
            "прошедшая"
        } else {
            status.displayName
        }

    val canCancel: Boolean
        get() = status == BookingStatus.ACTIVE && slot?.startAt?.after(Date()) == true
}

fun BookingStatus.isActive(): Boolean = this == BookingStatus.ACTIVE

val BookingStatus.displayName: String
    get() = when (this) {
        BookingStatus.ACTIVE -> "активна"
        BookingStatus.CANCELLED -> "отменена"
        BookingStatus.LATE_CANCEL -> "поздняя отмена"
        BookingStatus.STUDIO_CANCELLED -> "отменена студией"
    }
