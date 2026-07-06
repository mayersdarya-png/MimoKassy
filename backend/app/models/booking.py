from sqlalchemy import ForeignKey, Integer, DateTime, Enum, String
from sqlalchemy.orm import Mapped, mapped_column, relationship
from datetime import datetime
import enum
import uuid

from core.database import Base


class BookingStatus(str, enum.Enum):
    ACTIVE = "active"
    CANCELLED = "cancelled"
    LATE_CANCEL = "late_cancel"
    STUDIO_CANCELLED = "studio_cancelled"


class Booking(Base):
    __tablename__ = "bookings"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    slot_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("slots.id"), nullable=False)
    client_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("clients.id"), nullable=False)
    seats_count: Mapped[int] = mapped_column(Integer, nullable=False)
    rental_count: Mapped[int] = mapped_column(Integer, default=0)
    status: Mapped[BookingStatus] = mapped_column(Enum(BookingStatus), default=BookingStatus.ACTIVE)
    price_total: Mapped[int] = mapped_column(Integer, nullable=False)
    allergy_comment: Mapped[str] = mapped_column(String(500), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    cancelled_at: Mapped[datetime] = mapped_column(DateTime, nullable=True)
    cancellation_reason: Mapped[str] = mapped_column(String(200), nullable=True)

    slot = relationship("Slot", back_populates="bookings")
    client = relationship("Client", back_populates="bookings")
