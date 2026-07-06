from sqlalchemy import ForeignKey, DateTime, Integer, Enum, String, Float
from sqlalchemy.orm import Mapped, mapped_column, relationship
from datetime import datetime
import enum
import uuid

from core.database import Base


class SlotStatus(str, enum.Enum):
    SCHEDULED = "scheduled"
    CANCELLED = "cancelled"


class Slot(Base):
    __tablename__ = "slots"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    program_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("programs.id"), nullable=False)
    chef_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("chefs.id"), nullable=False)
    start_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, index=True)
    total_seats: Mapped[int] = mapped_column(Integer, nullable=False)
    free_seats: Mapped[int] = mapped_column(Integer, nullable=False)
    free_rental_kits: Mapped[int] = mapped_column(Integer, nullable=False)
    price: Mapped[int] = mapped_column(Integer, nullable=False)
    rental_price: Mapped[int] = mapped_column(Integer, default=0)
    studio_name: Mapped[str] = mapped_column(String(100), default="Шеф-стол")
    studio_address: Mapped[str] = mapped_column(String(200), nullable=False)
    studio_lat: Mapped[float] = mapped_column(Float, nullable=False)
    studio_lng: Mapped[float] = mapped_column(Float, nullable=False)
    status: Mapped[SlotStatus] = mapped_column(Enum(SlotStatus), default=SlotStatus.SCHEDULED)

    program = relationship("Program", back_populates="slots")
    chef = relationship("Chef", back_populates="slots")
    bookings = relationship("Booking", back_populates="slot")
