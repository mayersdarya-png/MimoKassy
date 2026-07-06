from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from datetime import timedelta

from core.database import get_db
from core.security import create_access_token, create_refresh_token
from schemas.auth import (
    RequestCodeRequest,
    RequestCodeResponse,
    VerifyCodeRequest,
    VerifyCodeResponse,
    TokenPair,
    ClientResponse,
    RefreshTokenRequest,
    PushTokenRequest,
    PushTokenDeleteRequest
)
from services.auth_service import auth_service
from api.deps import get_current_client
from models.client import Client
from utils.sms import send_sms

router = APIRouter()


@router.post("/request-code", response_model=RequestCodeResponse)
async def request_code(
        request: RequestCodeRequest,
        db: AsyncSession = Depends(get_db)
):
    """Запрос OTP кода"""
    # Генерируем код
    code = auth_service.generate_otp()

    # TODO: сохранить код в Redis с TTL

    # Отправляем SMS (заглушка)
    await send_sms(request.phone, f"Ваш код: {code}")

    return RequestCodeResponse(
        ttl_seconds=300,
        resend_after_seconds=60
    )


@router.post("/verify-code", response_model=VerifyCodeResponse)
async def verify_code(
        request: VerifyCodeRequest,
        db: AsyncSession = Depends(get_db)
):
    """Подтверждение кода и вход/регистрация"""
    # TODO: проверить код из Redis

    # Для MVP — код 123456 всегда работает
    if request.code != "123456":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Неверный код"
        )

    # Ищем клиента
    client = await auth_service.get_client_by_phone(db, request.phone)
    is_new = False

    if not client:
        # Создаём нового
        client = await auth_service.create_client(db, request.phone)
        is_new = True

    # Создаём токены
    access_token = create_access_token({"sub": str(client.id)})
    refresh_token = create_refresh_token({"sub": str(client.id)})

    return VerifyCodeResponse(
        tokens=TokenPair(
            access_token=access_token,
            refresh_token=refresh_token,
            expires_in=900
        ),
        client=ClientResponse(
            id=client.id,
            name=client.name,
            phone=client.phone,
            created_at=client.created_at.isoformat()
        ),
        is_new=is_new
    )


@router.post("/refresh")
async def refresh_token(
        request: RefreshTokenRequest,
        db: AsyncSession = Depends(get_db)
):
    """Обновление токена"""
    # TODO: проверить refresh_token в Redis
    payload = decode_token(request.refresh_token)

    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверный refresh токен"
        )

    client_id = payload.get("sub")
    client = await auth_service.get_client_by_id(db, client_id)

    if not client:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Пользователь не найден"
        )

    access_token = create_access_token({"sub": str(client.id)})
    refresh_token = create_refresh_token({"sub": str(client.id)})

    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "Bearer",
        "expires_in": 900
    }


@router.post("/logout")
async def logout(
        client: Client = Depends(get_current_client)
):
    """Выход из аккаунта"""
    # TODO: инвалидировать refresh_token в Redis
    return {"message": "Выход выполнен"}


@router.post("/push-tokens")
async def register_push_token(
        request: PushTokenRequest,
        client: Client = Depends(get_current_client)
):
    """Регистрация push-токена"""
    # TODO: сохранить push-токен в БД
    return {"message": "Push-токен зарегистрирован"}


@router.delete("/push-tokens")
async def delete_push_token(
        request: PushTokenDeleteRequest,
        client: Client = Depends(get_current_client)
):
    """Удаление push-токена"""
    # TODO: удалить push-токен из БД
    return {"message": "Push-токен удалён"}
