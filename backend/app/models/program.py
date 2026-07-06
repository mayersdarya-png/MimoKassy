from sqlalchemy import String, Enum, Integer
from sqlalchemy.orm import Mapped, mapped_column, relationship
import enum
import uuid

from core.database import Base


class ProgramType(str, enum.Enum):
    SIMPLE = "simple"
    COMPLEX = "complex"


class Program(Base):
    __tablename__ = "programs"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    description: Mapped[str] = mapped_column(String(500), nullable=True)
    type: Mapped[ProgramType] = mapped_column(Enum(ProgramType), nullable=False)
    duration_min: Mapped[int] = mapped_column(Integer, default=180)
    menu_description: Mapped[str] = mapped_column(String(500), nullable=True)

    slots = relationship("Slot", back_populates="program")
