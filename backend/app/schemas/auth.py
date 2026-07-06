from pydantic import BaseModel, Field, validator
from typing import Optional
from uuid import UUID
import re


class RequestCodeRequest(BaseModel):
    phone: str

    @validator('phone')
    def validate_phone(cls, v):
        if not re.match(r'^\+[1-9]\d{1,14}$', v):
            raise ValueError('Неверный формат телефона')
        return v


class RequestCodeResponse(BaseModel):
    ttl_seconds: int = 300
    resend_after_seconds: int = 60


class TokenPair(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "Bearer"
    expires_in: int = 900


class ClientResponse(BaseModel):
    id: UUID
    name: Optional[str] = None
    phone: str
    created_at: str


class VerifyCodeRequest(BaseModel):
    phone: str
    code: str

    @validator('phone')
    def validate_phone(cls, v):
        if not re.match(r'^\+[1-9]\d{1,14}$', v):
            raise ValueError('Неверный формат телефона')
        return v

    @validator('code')
    def validate_code(cls, v):
        if not re.match(r'^\d{4,6}$', v):
            raise ValueError('Код должен быть 4-6 цифр')
        return v


class VerifyCodeResponse(BaseModel):
    tokens: TokenPair
    client: ClientResponse
    is_new: bool


class RefreshTokenRequest(BaseModel):
    refresh_token: str


class PushTokenRequest(BaseModel):
    token: str
    platform: str

    @validator('platform')
    def validate_platform(cls, v):
        if v not in ['ios', 'android']:
            raise ValueError('Платформа должна быть ios или android')
        return v


class PushTokenDeleteRequest(BaseModel):
    token: str
    platform: str

    @validator('platform')
    def validate_platform(cls, v):
        if v not in ['ios', 'android']:
            raise ValueError('Платформа должна быть ios или android')
        return v
