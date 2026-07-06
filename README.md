
# Мимо Кассы 🍴

Клиент-серверное приложение для записи на кулинарные классы в студии «Шеф-стол».  
**Бэкенд:** Python + FastAPI + PostgreSQL  
**Фронтенд:** Android (Kotlin + Jetpack Compose)

---

## 📦 Репозиторий

```
mimo-kassy/
├── backend/          # Бэкенд (FastAPI + PostgreSQL + Redis)
├── android/          # Android-приложение (Kotlin + Compose)
├── api/              # OpenAPI-спецификация
├── docs/             # Документация (аналитика, ТЗ, дизайн)
└── README.md
```

---

## 🚀 Запуск бэкенда

### 1. Перейди в папку бэкенда
```bash
cd backend
```

### 2. Настрой переменные окружения
Скопируй `.env.example` в `.env`:
```bash
cp .env.example .env
```

### 3. Запусти через Docker
```bash
docker-compose up -d --build
```

### 4. Наполни базу тестовыми данными
```bash
docker exec -it mimo_kassy_backend python seed.py
```

### 5. Проверь работу
Открой в браузере:  
- Swagger: http://localhost:8000/docs  
- Health check: http://localhost:8000/health

---

## 📱 Запуск Android-приложения

1. Открой папку `android/MimoKassy` в **Android Studio**
2. Дождись синхронизации Gradle
3. Запусти эмулятор или подключи реальное устройство
4. Нажми **Run** ▶

> **Важно:** В `RetrofitInstance.kt` BASE_URL должен быть:
> - Для эмулятора: `http://10.0.2.2:8000/`
> - Для реального устройства: IP твоего компьютера в локальной сети

---

## 🧪 Тестовые данные для входа

| Поле | Значение |
|------|----------|
| **Телефон** | `+79991234567` |
| **Код** | `123456` |

---

### ✅ Что должно работать:

- [x] Регистрация/вход по телефону с кодом `123456`
- [x] Список доступных классов
- [x] Карточка класса с деталями (шеф, программа, цена, места)
- [x] Запись на класс (выбор мест и экипировки)
- [x] Список броней (предстоящие/прошедшие)
- [x] Отмена брони (ранняя/поздняя)
- [x] Профиль (имя, телефон, редактирование)
- [x] Выход и удаление аккаунта

---

## 🛠 Стек технологий

| Компонент | Технология |
|-----------|------------|
| **Бэкенд** | Python 3.12 + FastAPI + SQLAlchemy 2.0 |
| **База данных** | PostgreSQL 16 |
| **Кэш** | Redis 7 |
| **Контейнеризация** | Docker + docker-compose |
| **Фронтенд** | Kotlin + Jetpack Compose |
| **Сеть** | Retrofit 2 |
| **Авторизация** | JWT (access + refresh) |

---

## 📁 Структура бэкенда

```
backend/
├── app/
│   ├── api/v1/          # Эндпоинты (auth, slots, bookings, profile, chefs)
│   ├── core/            # Конфиг, БД, безопасность
│   ├── models/          # SQLAlchemy модели
│   ├── schemas/         # Pydantic схемы
│   ├── services/        # Бизнес-логика
│   ├── utils/           # Утилиты (SMS)
│   ├── main.py          # Точка входа FastAPI
│   └── seed.py          # Наполнение БД тестовыми данными
├── .env
├── docker-compose.yml
├── Dockerfile
└── requirements.txt
```

---

## 📁 Структура Android-приложения

```
android/MimoKassy/
├── app/src/main/java/com/mimokassy/
│   ├── data/
│   │   ├── api/          # Retrofit сервисы
│   │   └── models/       # Data классы
│   ├── navigation/       # NavGraph
│   ├── ui/
│   │   ├── auth/         # LoginScreen, OTPScreen
│   │   ├── bookings/     # BookingList, Detail, Form
│   │   ├── components/   # Loading, Error, Empty
│   │   ├── profile/      # ProfileScreen
│   │   ├── slots/        # SlotList, SlotCard
│   │   └── theme/        # Цвета, шрифты
│   ├── utils/
│   └── MainActivity.kt
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🔗 Полезные ссылки (после запуска)

| Что | URL |
|-----|-----|
| Swagger UI | http://localhost:8000/docs |
| ReDoc | http://localhost:8000/redoc |
| Health check | http://localhost:8000/health |

---

## ⚠️ Возможные проблемы и решения

### 1. Бэкенд не запускается
```bash
docker-compose down && docker-compose up -d --build
docker logs mimo_kassy_backend
```

### 2. Нет данных в списке классов
```bash
docker exec -it mimo_kassy_backend python seed.py
```

### 3. Ошибка подключения к БД
```bash
docker exec -it mimo_kassy_db psql -U postgres -d mimo_kassy -c "SELECT COUNT(*) FROM slots;"
```

### 4. Android не видит бэкенд
- **Эмулятор:** используй `10.0.2.2`
- **Реальное устройство:** проверь IP компьютера (`ifconfig` или `ipconfig`) и замени в `RetrofitInstance.kt`

---

**Версия:** 1.0.0  
**Дата:** 2026-07-06
```