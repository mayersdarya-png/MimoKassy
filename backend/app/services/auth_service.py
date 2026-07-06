from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID
from datetime import datetime
import random

from models.client import Client


class AuthService:

    async def get_client_by_id(self, db: AsyncSession, client_id: UUID) -> Client | None:
        """Получение клиента по ID"""
        result = await db.execute(
            select(Client).where(Client.id == client_id)
        )
        return result.scalar_one_or_none()

    async def get_client_by_phone(self, db: AsyncSession, phone: str) -> Client | None:
        """Получение клиента по телефону"""
        result = await db.execute(
            select(Client).where(Client.phone == phone)
        )
        return result.scalar_one_or_none()

    async def create_client(self, db: AsyncSession, phone: str) -> Client:
        """Создание нового клиента"""
        client = Client(
            phone=phone,
            name=None,
            created_at=datetime.utcnow()
        )
        db.add(client)
        await db.commit()
        await db.refresh(client)
        return client

    async def update_client_name(self, db: AsyncSession, client_id: UUID, name: str) -> Client:
        """Обновление имени клиента"""
        client = await self.get_client_by_id(db, client_id)
        if client:
            client.name = name
            await db.commit()
            await db.refresh(client)
        return client

    async def delete_client(self, db: AsyncSession, client_id: UUID) -> None:
        """Удаление клиента"""
        client = await self.get_client_by_id(db, client_id)
        if client:
            # TODO: анонимизация броней
            await db.delete(client)
            await db.commit()

    def generate_otp(self) -> str:
        """Генерация OTP кода"""
        return str(random.randint(100000, 999999))


auth_service = AuthService()
