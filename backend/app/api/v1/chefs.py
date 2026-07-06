from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from core.database import get_db
from models.chef import Chef
from api.deps import get_current_client

router = APIRouter()


@router.get("/")
async def list_chefs(
        db: AsyncSession = Depends(get_db),
        client: Chef = Depends(get_current_client)
):
    """Список шефов"""
    result = await db.execute(select(Chef))
    chefs = result.scalars().all()

    return {
        "items": [
            {
                "id": chef.id,
                "name": chef.name
            }
            for chef in chefs
        ],
        "meta": {
            "limit": 100,
            "offset": 0,
            "total": len(chefs)
        }
    }
