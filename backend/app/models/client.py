from sqlalchemy import String, DateTime
from sqlalchemy.orm import Mapped, mapped_column, relationship
from datetime import datetime
import uuid

from core.database import Base


class Client(Base):
    __tablename__ = "clients"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    name: Mapped[str] = mapped_column(String(100), nullable=True)
    phone: Mapped[str] = mapped_column(String(20), unique=True, nullable=False, index=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    bookings = relationship("Booking", back_populates="client", cascade="all, delete-orphan")
