from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID
from datetime import datetime


class CreateBookingRequest(BaseModel):
    slot_id: UUID
    seats_count: int = Field(..., ge=1, le=3)
    rental_count: int = Field(..., ge=0, le=3)
    allergy_comment: Optional[str] = Field(None, max_length=500)


class BookingStatus(str):
    ACTIVE = "active"
    CANCELLED = "cancelled"
    LATE_CANCEL = "late_cancel"
    STUDIO_CANCELLED = "studio_cancelled"


class BookingResponse(BaseModel):
    id: UUID
    slot_id: UUID
    client_id: UUID
    seats_count: int
    rental_count: int
    status: str
    price_total: int
    allergy_comment: Optional[str]
    created_at: datetime
    cancelled_at: Optional[datetime]
    cancellation_reason: Optional[str]
    slot: Optional[dict]  # Вложенный слот


class CreateBookingResponse(BookingResponse):
    is_first_booking: bool
    reminder_hours: List[int] = [24, 2]


class BookingListResponse(BaseModel):
    items: List[BookingResponse]
    meta: dict
