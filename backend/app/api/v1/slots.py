from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from core.database import get_db
from models.slot import Slot

router = APIRouter()


@router.get("/")
async def list_slots(db: AsyncSession = Depends(get_db)):
    # ✅ ЖАДНАЯ ЗАГРУЗКА (решает проблему MissingGreenlet)
    result = await db.execute(
        select(Slot).options(
            selectinload(Slot.program),
            selectinload(Slot.chef)
        )
    )
    slots = result.scalars().all()

    return {
        "items": [
            {
                "id": str(slot.id),
                "start_at": slot.start_at.isoformat(),
                "program": {
                    "id": str(slot.program.id),
                    "name": slot.program.name,
                    "type": slot.program.type.value,
                    "duration_min": slot.program.duration_min,
                    "menu_description": slot.program.menu_description
                },
                "chef": {
                    "id": str(slot.chef.id),
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
            }
            for slot in slots
        ],
        "total": len(slots)
    }


@router.get("/{slot_id}")
async def get_slot(slot_id: str, db: AsyncSession = Depends(get_db)):
    from uuid import UUID
    try:
        slot_uuid = UUID(slot_id)
    except ValueError:
        return {"error": "Invalid slot_id"}

    # ✅ ТОЖЕ С ЖАДНОЙ ЗАГРУЗКОЙ
    result = await db.execute(
        select(Slot)
        .where(Slot.id == slot_uuid)
        .options(
            selectinload(Slot.program),
            selectinload(Slot.chef)
        )
    )
    slot = result.scalar_one_or_none()

    if not slot:
        return {"error": "Slot not found"}

    return {
        "id": str(slot.id),
        "start_at": slot.start_at.isoformat(),
        "program": {
            "id": str(slot.program.id),
            "name": slot.program.name,
            "type": slot.program.type.value,
            "duration_min": slot.program.duration_min,
            "menu_description": slot.program.menu_description
        },
        "chef": {
            "id": str(slot.chef.id),
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
    }
