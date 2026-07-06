import asyncio
from datetime import datetime, timedelta
import uuid
import random

from core.database import AsyncSessionLocal
from models.client import Client
from models.program import Program, ProgramType
from models.chef import Chef
from models.slot import Slot, SlotStatus


async def seed():
    async with AsyncSessionLocal() as db:
        print("🌱 Начинаем наполнение базы данных...")

        # ============================================
        # 1. Шефы
        # ============================================
        chefs = [
            Chef(id=uuid.uuid4(), name="Анна"),
            Chef(id=uuid.uuid4(), name="Игорь"),
            Chef(id=uuid.uuid4(), name="Мария"),
            Chef(id=uuid.uuid4(), name="Олег"),
            Chef(id=uuid.uuid4(), name="Екатерина"),
        ]
        db.add_all(chefs)
        await db.flush()
        print(f"✅ Добавлено {len(chefs)} шефов")

        # ============================================
        # 2. Программы
        # ============================================
        programs = [
            Program(
                id=uuid.uuid4(),
                name="Итальянская кухня",
                description="Классические рецепты итальянской кухни для новичков",
                type=ProgramType.SIMPLE,
                duration_min=180,
                menu_description="Паста Карбонара, Тирамису"
            ),
            Program(
                id=uuid.uuid4(),
                name="Японская кухня",
                description="Суши, роллы и другие японские блюда",
                type=ProgramType.SIMPLE,
                duration_min=180,
                menu_description="Суши, Роллы, Мисо-суп"
            ),
            Program(
                id=uuid.uuid4(),
                name="Французская выпечка",
                description="Мастер-класс по французской выпечке с духовкой",
                type=ProgramType.COMPLEX,
                duration_min=210,
                menu_description="Круассаны, Макаруны"
            ),
            Program(
                id=uuid.uuid4(),
                name="Мясная кухня",
                description="Приготовление мясных блюд с использованием сувида",
                type=ProgramType.COMPLEX,
                duration_min=200,
                menu_description="Стейк Рибай, Соус Бернез"
            ),
        ]
        db.add_all(programs)
        await db.flush()
        print(f"✅ Добавлено {len(programs)} программ")

        # ============================================
        # 3. Слоты (на 7 дней вперёд, по 3 в день)
        # ============================================
        now = datetime.utcnow()
        slots = []
        slot_count = 0

        for day in range(7):
            for hour in [10, 14, 18]:  # 10:00, 14:00, 18:00
                start_at = now + timedelta(days=day, hours=hour - now.hour, minutes=-now.minute, seconds=-now.second)
                if start_at < now:
                    start_at += timedelta(days=1)

                program = random.choice(programs)
                chef = random.choice(chefs)
                total_seats = 12 if program.type == ProgramType.SIMPLE else 8
                free_seats = random.randint(0, total_seats)
                free_rental_kits = random.randint(0, 12)

                slots.append(
                    Slot(
                        id=uuid.uuid4(),
                        program_id=program.id,
                        chef_id=chef.id,
                        start_at=start_at,
                        total_seats=total_seats,
                        free_seats=free_seats,
                        free_rental_kits=free_rental_kits,
                        price=3000,
                        rental_price=500,
                        studio_name="Шеф-стол",
                        studio_address="ул. Серпуховская, д. 12, лофт 3",
                        studio_lat=55.7558,
                        studio_lng=37.6173,
                        status=SlotStatus.SCHEDULED
                    )
                )
                slot_count += 1

        db.add_all(slots)
        await db.flush()
        print(f"✅ Добавлено {slot_count} слотов")

        # ============================================
        # 4. Клиенты (для тестов)
        # ============================================
        clients = [
            Client(id=uuid.uuid4(), name="Анна Петрова", phone="+79991234567", created_at=datetime.utcnow()),
            Client(id=uuid.uuid4(), name="Иван Иванов", phone="+79997654321", created_at=datetime.utcnow()),
            Client(id=uuid.uuid4(), name=None, phone="+79998887766", created_at=datetime.utcnow()),
        ]
        db.add_all(clients)
        await db.flush()
        print(f"✅ Добавлено {len(clients)} клиентов")

        # ============================================
        # 5. Коммит
        # ============================================
        await db.commit()
        print("🎉 База данных успешно заполнена!")


if __name__ == "__main__":
    asyncio.run(seed())
