from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from core.database import engine, Base
from core.config import settings
from api.v1 import auth, slots, bookings, profile, chefs


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    await engine.dispose()


app = FastAPI(
    title="Мимо Кассы API",
    description="Клиентское API для записи на кулинарные классы",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router, prefix="/auth", tags=["Auth"])
app.include_router(slots.router, prefix="/slots", tags=["Slots"])
app.include_router(bookings.router, prefix="/bookings", tags=["Bookings"])
app.include_router(profile.router, prefix="/profile", tags=["Profile"])
app.include_router(chefs.router, prefix="/chefs", tags=["Chefs"])


@app.get("/health")
async def health_check():
    return {"status": "ok", "service": "mimo-kassy-backend"}
