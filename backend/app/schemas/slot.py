from pydantic import BaseModel, Field, validator
from typing import Optional, List
from uuid import UUID
from datetime import datetime


class CreateBookingRequest(BaseModel):
    slot_id: UUID
    seats_count: int
    rental_count: int
    allergy_comment: Optional[str] = None

    @validator('seats_count')
    def validate_seats(cls, v):
        if v < 1 or v > 3:
            raise ValueError('Количество мест должно быть от 1 до 3')
        return v

    @validator('rental_count')
    def validate_rental(cls, v):
        if v < 0 or v > 3:
            raise ValueError('Количество проката должно быть от 0 до 3')
        return v

    @validator('allergy_comment')
    def validate_allergy(cls, v):
        if v and len(v) > 500:
            raise ValueError('Комментарий не должен превышать 500 символов')
        return v


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
    slot: Optional[dict]


class CreateBookingResponse(BookingResponse):
    is_first_booking: bool
    reminder_hours: List[int] = [24, 2]


class BookingListResponse(BaseModel):
    items: List[BookingResponse]
    meta: dict
