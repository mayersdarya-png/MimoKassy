from fastapi import APIRouter, Depends, HTTPException, status, Header, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from typing import Optional, List
from uuid import UUID
from datetime import datetime
import uuid

from core.database import get_db
from models.slot import Slot, SlotStatus
from models.booking import Booking, BookingStatus
from models.client import Client
from api.deps import get_current_client
from schemas.booking import CreateBookingRequest

router = APIRouter()


@router.post("/")
async def create_booking(
        request: CreateBookingRequest,
        idempotency_key: str = Header(..., description="Ключ идемпотентности"),
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    # ✅ ИСПРАВЛЕНО — с selectinload для program и chef
    result = await db.execute(
        select(Slot)
        .where(Slot.id == request.slot_id)
        .options(
            selectinload(Slot.program),
            selectinload(Slot.chef)
        )
    )
    slot = result.scalar_one_or_none()

    if not slot:
        raise HTTPException(status_code=404, detail="Слот не найден")

    if slot.status == SlotStatus.CANCELLED:
        raise HTTPException(status_code=410, detail="Слот отменён")

    if slot.start_at < datetime.utcnow():
        raise HTTPException(status_code=422, detail="Слот уже начался")

    if slot.free_seats < request.seats_count:
        raise HTTPException(
            status_code=409,
            detail={"code": "slot_full", "message": f"Недостаточно мест. Свободно: {slot.free_seats}"}
        )

    if request.rental_count > slot.free_rental_kits:
        raise HTTPException(
            status_code=409,
            detail={"code": "slot_full",
                    "message": f"Недостаточно прокатной экипировки. Свободно: {slot.free_rental_kits}"}
        )

    existing = await db.execute(
        select(Booking).where(
            Booking.slot_id == request.slot_id,
            Booking.client_id == client.id,
            Booking.status == BookingStatus.ACTIVE
        )
    )
    if existing.scalar_one_or_none():
        raise HTTPException(status_code=409,
                            detail={"code": "double_booking", "message": "У вас уже есть бронь на этот слот"})

    price_total = slot.price * request.seats_count + slot.rental_price * request.rental_count

    booking = Booking(
        id=uuid.uuid4(),
        slot_id=request.slot_id,
        client_id=client.id,
        seats_count=request.seats_count,
        rental_count=request.rental_count,
        status=BookingStatus.ACTIVE,
        price_total=price_total,
        allergy_comment=request.allergy_comment,
        created_at=datetime.utcnow()
    )

    db.add(booking)
    slot.free_seats -= request.seats_count
    slot.free_rental_kits -= request.rental_count

    await db.commit()
    await db.refresh(booking)

    all_bookings = await db.execute(select(Booking).where(Booking.client_id == client.id))
    is_first_booking = len(all_bookings.scalars().all()) == 1

    return {
        "id": booking.id,
        "slot_id": booking.slot_id,
        "client_id": booking.client_id,
        "seats_count": booking.seats_count,
        "rental_count": booking.rental_count,
        "status": booking.status.value,
        "price_total": booking.price_total,
        "allergy_comment": booking.allergy_comment,
        "created_at": booking.created_at.isoformat(),
        "cancelled_at": None,
        "cancellation_reason": None,
        "slot": {
            "id": slot.id,
            "start_at": slot.start_at.isoformat(),
            "program": {
                "id": slot.program.id,
                "name": slot.program.name,
                "description": slot.program.description,
                "type": slot.program.type.value,
                "duration_min": slot.program.duration_min,
                "menu_description": slot.program.menu_description
            },
            "chef": {
                "id": slot.chef.id,
                "name": slot.chef.name
            },
            "total_seats": slot.total_seats,
            "free_seats": slot.free_seats,
            "free_rental_kits": slot.free_rental_kits,
            "price": slot.price,
            "rental_price": slot.rental_price,
            "studio_name": slot.studio_name,
            "studio_address": slot.studio_address,
            "studio_lat": slot.studio_lat,
            "studio_lng": slot.studio_lng,
            "status": slot.status.value
        },
        "is_first_booking": is_first_booking,
        "reminder_hours": [24, 2]
    }


@router.get("/")
async def list_bookings(
        status: Optional[List[str]] = Query(None),
        limit: int = Query(20, ge=1, le=100),
        offset: int = Query(0, ge=0),
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    query = select(Booking).where(Booking.client_id == client.id).options(
        selectinload(Booking.slot).selectinload(Slot.program),
        selectinload(Booking.slot).selectinload(Slot.chef)
    )
    if status:
        query = query.where(Booking.status.in_(status))
    query = query.order_by(Booking.created_at.desc()).offset(offset).limit(limit)
    result = await db.execute(query)
    bookings = result.scalars().all()

    return {
        "items": [
            {
                "id": b.id,
                "slot_id": b.slot_id,
                "client_id": b.client_id,
                "seats_count": b.seats_count,
                "rental_count": b.rental_count,
                "status": b.status.value,
                "price_total": b.price_total,
                "allergy_comment": b.allergy_comment,
                "created_at": b.created_at.isoformat(),
                "cancelled_at": b.cancelled_at.isoformat() if b.cancelled_at else None,
                "cancellation_reason": b.cancellation_reason,
                "slot": {
                    "id": b.slot.id,
                    "start_at": b.slot.start_at.isoformat(),
                    "program": {
                        "id": b.slot.program.id,
                        "name": b.slot.program.name,
                        "description": b.slot.program.description,
                        "type": b.slot.program.type.value,
                        "duration_min": b.slot.program.duration_min,
                        "menu_description": b.slot.program.menu_description
                    },
                    "chef": {
                        "id": b.slot.chef.id,
                        "name": b.slot.chef.name
                    },
                    "total_seats": b.slot.total_seats,
                    "free_seats": b.slot.free_seats,
                    "free_rental_kits": b.slot.free_rental_kits,
                    "price": b.slot.price,
                    "rental_price": b.slot.rental_price,
                    "studio_name": b.slot.studio_name,
                    "studio_address": b.slot.studio_address,
                    "studio_lat": b.slot.studio_lat,
                    "studio_lng": b.slot.studio_lng,
                    "status": b.slot.status.value
                }
            }
            for b in bookings
        ],
        "meta": {"limit": limit, "offset": offset, "total": len(bookings)}
    }


@router.get("/{booking_id}")
async def get_booking(
        booking_id: UUID,
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    result = await db.execute(
        select(Booking)
        .where(Booking.id == booking_id, Booking.client_id == client.id)
        .options(
            selectinload(Booking.slot).selectinload(Slot.program),
            selectinload(Booking.slot).selectinload(Slot.chef)
        )
    )
    booking = result.scalar_one_or_none()
    if not booking:
        raise HTTPException(status_code=404, detail="Бронь не найдена")

    return {
        "id": booking.id,
        "slot_id": booking.slot_id,
        "client_id": booking.client_id,
        "seats_count": booking.seats_count,
        "rental_count": booking.rental_count,
        "status": booking.status.value,
        "price_total": booking.price_total,
        "allergy_comment": booking.allergy_comment,
        "created_at": booking.created_at.isoformat(),
        "cancelled_at": booking.cancelled_at.isoformat() if booking.cancelled_at else None,
        "cancellation_reason": booking.cancellation_reason,
        "slot": {
            "id": booking.slot.id,
            "start_at": booking.slot.start_at.isoformat(),
            "program": {
                "id": booking.slot.program.id,
                "name": booking.slot.program.name,
                "description": booking.slot.program.description,
                "type": booking.slot.program.type.value,
                "duration_min": booking.slot.program.duration_min,
                "menu_description": booking.slot.program.menu_description
            },
            "chef": {
                "id": booking.slot.chef.id,
                "name": booking.slot.chef.name
            },
            "total_seats": booking.slot.total_seats,
            "free_seats": booking.slot.free_seats,
            "free_rental_kits": booking.slot.free_rental_kits,
            "price": booking.slot.price,
            "rental_price": booking.slot.rental_price,
            "studio_name": booking.slot.studio_name,
            "studio_address": booking.slot.studio_address,
            "studio_lat": booking.slot.studio_lat,
            "studio_lng": booking.slot.studio_lng,
            "status": booking.slot.status.value
        }
    }


@router.post("/{booking_id}/cancel")
async def cancel_booking(
        booking_id: UUID,
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    result = await db.execute(
        select(Booking)
        .where(Booking.id == booking_id, Booking.client_id == client.id)
        .options(
            selectinload(Booking.slot).selectinload(Slot.program),
            selectinload(Booking.slot).selectinload(Slot.chef)
        )
    )
    booking = result.scalar_one_or_none()

    if not booking:
        raise HTTPException(status_code=404, detail="Бронь не найдена")

    if booking.status != BookingStatus.ACTIVE:
        raise HTTPException(status_code=409, detail="Запись уже отменена")

    if booking.slot.start_at < datetime.utcnow():
        raise HTTPException(status_code=422, detail="Слот уже начался")

    hours_left = (booking.slot.start_at - datetime.utcnow()).total_seconds() / 3600

    if hours_left >= 24:
        booking.status = BookingStatus.CANCELLED
        booking.cancelled_at = datetime.utcnow()
        booking.slot.free_seats += booking.seats_count
        booking.slot.free_rental_kits += booking.rental_count
    else:
        booking.status = BookingStatus.LATE_CANCEL
        booking.cancelled_at = datetime.utcnow()

    await db.commit()
    await db.refresh(booking)

    return {
        "id": booking.id,
        "slot_id": booking.slot_id,
        "client_id": booking.client_id,
        "seats_count": booking.seats_count,
        "rental_count": booking.rental_count,
        "status": booking.status.value,
        "price_total": booking.price_total,
        "allergy_comment": booking.allergy_comment,
        "created_at": booking.created_at.isoformat(),
        "cancelled_at": booking.cancelled_at.isoformat(),
        "cancellation_reason": booking.cancellation_reason,
        "slot": {
            "id": booking.slot.id,
            "start_at": booking.slot.start_at.isoformat(),
            "program": {
                "id": booking.slot.program.id,
                "name": booking.slot.program.name,
                "description": booking.slot.program.description,
                "type": booking.slot.program.type.value,
                "duration_min": booking.slot.program.duration_min,
                "menu_description": booking.slot.program.menu_description
            },
            "chef": {
                "id": booking.slot.chef.id,
                "name": booking.slot.chef.name
            },
            "total_seats": booking.slot.total_seats,
            "free_seats": booking.slot.free_seats,
            "free_rental_kits": booking.slot.free_rental_kits,
            "price": booking.slot.price,
            "rental_price": booking.slot.rental_price,
            "studio_name": booking.slot.studio_name,
            "studio_address": booking.slot.studio_address,
            "studio_lat": booking.slot.studio_lat,
            "studio_lng": booking.slot.studio_lng,
            "status": booking.slot.status.value
        }
    }
