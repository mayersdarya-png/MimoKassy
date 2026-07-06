from pydantic import BaseModel, Field, validator
from typing import Optional
from uuid import UUID
import re


class UpdateProfileRequest(BaseModel):
    name: str

    @validator('name')
    def validate_name(cls, v):
        if len(v) < 1 or len(v) > 100:
            raise ValueError('Имя должно быть от 1 до 100 символов')
        return v


class ChangePhoneRequest(BaseModel):
    new_phone: str

    @validator('new_phone')
    def validate_phone(cls, v):
        if not re.match(r'^\+[1-9]\d{1,14}$', v):
            raise ValueError('Неверный формат телефона')
        return v


class ChangePhoneConfirmRequest(BaseModel):
    new_phone: str
    code: str

    @validator('new_phone')
    def validate_phone(cls, v):
        if not re.match(r'^\+[1-9]\d{1,14}$', v):
            raise ValueError('Неверный формат телефона')
        return v

    @validator('code')
    def validate_code(cls, v):
        if not re.match(r'^\d{4,6}$', v):
            raise ValueError('Код должен быть 4-6 цифр')
        return v
