# Баги, найденные и исправленные в проекте «Мимо Кассы»

**Проект:** Клиент-серверное приложение для записи на кулинарные классы  
**Дата:** 2026-07-06  
**Всего багов:** 3

---

## 🐛 БАГ №1: Ошибка MissingGreenlet в эндпоинте `/slots`

### Симптом
При запросе `GET /slots` бэкенд возвращал `500 Internal Server Error` с ошибкой:

```
sqlalchemy.exc.MissingGreenlet: greenlet_spawn has not been called; 
can't call await_only() here. Was IO attempted in an unexpected place?
```

### Где проявлялось
- В Swagger UI при вызове `GET /slots`
- В Android-приложении при загрузке списка классов

### Требования (из ТЗ)
- **FR-9:** Система должна показывать клиенту список предстоящих слотов
- **FR-9a:** Система должна показывать клиенту карточку слота со всеми параметрами
- **NFR-8:** Защита от овербукинга

### Причина
SQLAlchemy пытался загрузить связанные данные (`program` и `chef`) лениво (lazy loading) внутри асинхронной функции. В асинхронном режиме (asyncpg) lazy loading не поддерживается и вызывает ошибку `MissingGreenlet`.

### Решение
В `app/api/v1/slots.py` добавлена жадная загрузка (`selectinload`) для связей `program` и `chef`:

**Было (ошибка):**
```python
result = await db.execute(select(Slot).where(Slot.status == SlotStatus.SCHEDULED))
```

**Стало (исправлено):**
```python
result = await db.execute(
    select(Slot)
    .where(Slot.status == SlotStatus.SCHEDULED)
    .options(
        selectinload(Slot.program),
        selectinload(Slot.chef)
    )
)
```

### Исправленные файлы
- `app/api/v1/slots.py` (функция `list_slots`)
- `app/api/v1/slots.py` (функция `get_slot`)

### Промпты ИИ
1. *"ошибка MissingGreenlet в slots.py, как исправить? Бэкенд на FastAPI + SQLAlchemy 2.0, асинхронный режим"*
2. *"нужна жадная загрузка для program и chef в SQLAlchemy, чтобы избежать lazy loading в асинхронном контексте"*

### Проверка
- [x] `GET /slots` возвращает 200 OK
- [x] Список слотов содержит данные program и chef
- [x] Swagger показывает корректный ответ
- [x] Android-приложение показывает список классов

### Commit
```
fix: add selectinload to slots endpoint to fix MissingGreenlet error
```

---

## 🐛 БАГ №2: Ошибка MissingGreenlet в эндпоинте `/bookings`

### Симптом
При запросе `GET /bookings` бэкенд возвращал `500 Internal Server Error` с ошибкой:

```
sqlalchemy.exc.MissingGreenlet: greenlet_spawn has not been called
```

Сопутствующая ошибка:
```
AttributeError: Neither 'InstrumentedAttribute' object nor 'Comparator' object 
associated with Booking.slot has an attribute 'program'
```

### Где проявлялось
- В Swagger UI при вызове `GET /bookings`
- В Android-приложении при открытии вкладки «Мои записи»

### Требования (из ТЗ)
- **FR-35a:** Система должна показывать клиенту список его броней (предстоящие/прошедшие)
- **FR-16:** Просмотр деталей своей записи
- **UC-2:** Отмена записи

### Причина
В функциях `list_bookings`, `get_booking` и `cancel_booking` отсутствовала жадная загрузка для вложенных связей `slot.program` и `slot.chef`. Кроме того, использование `selectinload(Booking.slot.program)` было синтаксически неверным — SQLAlchemy не поддерживает вложенные `selectinload` в одном вызове.

### Решение
В `app/api/v1/bookings.py` добавлена правильная жадная загрузка через цепочку `.selectinload()`:

**Было (ошибка):**
```python
query = select(Booking).where(Booking.client_id == client.id)
```

**Стало (исправлено):**
```python
query = select(Booking).where(Booking.client_id == client.id).options(
    selectinload(Booking.slot).selectinload(Slot.program),
    selectinload(Booking.slot).selectinload(Slot.chef)
)
```

### Исправленные файлы
- `app/api/v1/bookings.py` (функции `list_bookings`, `get_booking`, `cancel_booking`)

### Промпты ИИ
1. *"ошибка MissingGreenlet в bookings.py, бэкенд на FastAPI + SQLAlchemy 2.0, асинхронный режим"*
2. *"AttributeError: Neither 'InstrumentedAttribute' object nor 'Comparator' object associated with Booking.slot has an attribute 'program' — как правильно использовать selectinload для вложенных связей?"*


### Проверка
- [x] `GET /bookings` возвращает 200 OK
- [x] Список броней содержит данные slot, program и chef
- [x] `GET /bookings/{id}` возвращает полные данные
- [x] `POST /bookings/{id}/cancel` работает корректно
- [x] Android-приложение показывает список броней

### Commit
```
fix: add selectinload to bookings endpoints to fix MissingGreenlet error
```

---

## 🐛 БАГ №3: Ошибка приведения String к Date в Android

### Симптом
При открытии вкладки «Мои записи» Android-приложение падало с ошибкой:

```
java.lang.String cannot be cast to java.util.Date
```

### Где проявлялось
- Android-приложение, вкладка «Мои записи» (`BookingListScreen`)
- При попытке отобразить дату создания брони

### Требования (из ТЗ)
- **FR-35a:** Список своих броней
- **Модель данных:** `Booking.created_at` и `Booking.cancelled_at`

### Причина
В модели данных `Booking.kt` поля `createdAt` и `cancelledAt` были объявлены как `Date`, но с бэкенда приходит строка в формате ISO 8601 (например, `"2026-07-05T23:32:06.792Z"`). При попытке парсинга JSON в Kotlin возникало исключение.

### Решение
Типы полей в `Booking.kt` изменены с `Date` на `String`:

**Было (ошибка):**
```kotlin
data class Booking(
    // ...
    val createdAt: Date,
    val cancelledAt: Date?,
    // ...
)
```

**Стало (исправлено):**
```kotlin
data class Booking(
    // ...
    val createdAt: String,
    val cancelledAt: String?,
    // ...
)
```

### Исправленные файлы
- `android/MimoKassy/app/src/main/java/com/mimokassy/data/models/Booking.kt`

### Промпты ИИ
1. *"java.lang.String cannot be cast to java.util.Date в Booking при открытии списка броней в Android"*
2. *"ошибка при открытии вкладки записи android, падает с ClassCastException"*
3. *"исправь Booking.kt, поменяй тип полей createdAt и cancelledAt на String"*

### Проверка
- [x] Список броней открывается без ошибок
- [x] Даты отображаются корректно
- [x] Отмена брони работает
- [x] Детали брони открываются

### Commit
```
fix: change createdAt and cancelledAt types from Date to String in Booking model
```

---

## 📊 Сводная таблица багов

| # | Название | Компонент | Статус | Приоритет |
|---|----------|-----------|--------|-----------|
| 1 | MissingGreenlet в `/slots` | Бэкенд (FastAPI) | ✅ Исправлен | Critical |
| 2 | MissingGreenlet в `/bookings` | Бэкенд (FastAPI) | ✅ Исправлен | Critical |
| 3 | String → Date в Android | Фронтенд (Android) | ✅ Исправлен | Critical |

---

## 📁 Исправленные файлы

| Файл | Изменения |
|------|-----------|
| `backend/app/api/v1/slots.py` | Добавлен `selectinload(Slot.program)` и `selectinload(Slot.chef)` |
| `backend/app/api/v1/bookings.py` | Добавлен `selectinload(Booking.slot).selectinload(Slot.program)` и `selectinload(Booking.slot).selectinload(Slot.chef)` |
| `android/.../Booking.kt` | `createdAt` и `cancelledAt` изменены с `Date` на `String` |

---

**Все баги исправлены. Проект работает стабильно.** ✅
```