from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from core.database import get_db
from api.deps import get_current_client
from models.client import Client
from services.auth_service import auth_service
from schemas.profile import UpdateProfileRequest, ChangePhoneRequest, ChangePhoneConfirmRequest
from utils.sms import send_sms

router = APIRouter()


@router.get("/")
async def get_profile(
        client: Client = Depends(get_current_client)
):
    """Получение профиля"""
    return {
        "id": client.id,
        "name": client.name,
        "phone": client.phone,
        "created_at": client.created_at.isoformat()
    }


@router.patch("/")
async def update_profile(
        request: UpdateProfileRequest,
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    """Обновление имени"""
    client.name = request.name
    await db.commit()
    await db.refresh(client)

    return {
        "id": client.id,
        "name": client.name,
        "phone": client.phone,
        "created_at": client.created_at.isoformat()
    }


@router.delete("/")
async def delete_account(
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    """Удаление аккаунта"""
    await auth_service.delete_client(db, client.id)
    return {"message": "Аккаунт удалён"}


@router.post("/phone/request-code")
async def request_phone_change_code(
        request: ChangePhoneRequest,
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    """Запрос кода для смены телефона"""
    # TODO: сохранить код в Redis
    code = "654321"

    await send_sms(request.new_phone, f"Ваш код для смены номера: {code}")

    return {
        "ttl_seconds": 300,
        "resend_after_seconds": 60
    }


@router.post("/phone/confirm")
async def confirm_phone_change(
        request: ChangePhoneConfirmRequest,
        db: AsyncSession = Depends(get_db),
        client: Client = Depends(get_current_client)
):
    """Подтверждение смены телефона"""
    # TODO: проверить код из Redis

    # Проверяем, не занят ли номер
    existing = await auth_service.get_client_by_phone(db, request.new_phone)
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Номер уже занят"
        )

    client.phone = request.new_phone
    await db.commit()
    await db.refresh(client)

    return {
        "id": client.id,
        "name": client.name,
        "phone": client.phone,
        "created_at": client.created_at.isoformat()
    }
