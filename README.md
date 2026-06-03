# Queue project

Queue Project — это веб-приложение для создания событий и управления очередями.

Основные возможности:

- создание групп
- создание событий
- запись пользователей в очередь
- управление участниками очереди
- авторизация и регистрация пользователей (JWT)
- просмотр и управление активными очередями

---

## 🌐 Демо

Приложение доступно онлайн:

👉 https://qapp.space


---

## 🧱 Архитектура

Система состоит из трёх основных компонентов:

- **Frontend** — пользовательский интерфейс (HTML, CSS, JavaScript)
- **Backend** — REST API на Spring Boot
- **Database** — PostgreSQL

Все сервисы запускаются в Docker-контейнерах и взаимодействуют через внутреннюю сеть Docker.

```
Frontend → Backend (REST API) → PostgreSQL
```

---

## ⚙️ Технологии

### Backend
- Java 21
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL

### Frontend
- HTML5
- CSS3
- JavaScript (Vanilla)

### DevOps
- Docker
- Docker Compose
- Multi-stage Docker build
- Environment variables (.env)


---

## Требования

- Docker Engine 24+
- Docker Compose

Проверить установку:

```bash
docker --version
docker compose version
```

> Java, Maven и PostgreSQL устанавливать не нужно — всё запускается внутри контейнеров.

---

## 📁 Переменные окружения

Перед запуском необходимо создать файл `.env` в корне проекта:

```env
POSTGRES_DB=queue_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

JWT_SECRET=your-secret-key

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/queue_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

```

---

## 🚀 Локальный запуск

### 1. Клонирование проекта

```bash
git clone https://github.com/KkonepHuk/queue-project.git
cd queue-project
```

---

### 2. Запуск всех сервисов

```bash
docker compose up --build -d
```

Запустятся:

- frontend
- backend
- postgres

---

### 3. Проверка

```bash
docker ps
```

---

### 4. Доступ
Локальное приложение будет доступно на

```
http://localhost:8080
```

---

## ⛔ Остановка

```bash
docker compose down
```

Удалить данные:

```bash
docker compose down -v
```