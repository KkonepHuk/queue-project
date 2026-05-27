# Queue project

Проект реализует систему для создания событий и менеджмента очередейю.

---

## 🔹 Требования

- Java 17+
- Maven
- Docker
- PostgreSQL

---

## 🔹 Развёртывание проекта через Docker

### Склонируй репозиторий

```bash
git clone <repo_url>
cd queue_project
```

### Запуск PostgreSQL через Docker

```bash
docker compose up -d
```

Это поднимет контейнер PostgreSQL и настроит базу данных согласно `docker-compose.yml`.

### Проверка запущенных контейнеров

```bash
docker ps
```

Убедись, что контейнер с базой **queue_db** запущен и слушает порт **5433**.

### Остановка и удаление контейнеров

```bash
docker compose down -v
```

---

## 🔹 Запуск Spring Boot приложения

### Собрать проект

```bash
mvn clean install
```

### Запустить приложение

```bash
mvn spring-boot:run
```

Сервер будет доступен по адресу:

```
http://localhost:8080
```

---

## 🔹 Тестирование API

### Регистрация пользователя

```
POST /api/users/register
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Логин

```
POST /api/users/login
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

Используй **Postman** для отправки запросов.

---

## 🔹 Подключение к базе данных вручную

Если база работает через Docker:

```bash
psql -h localhost -p 5433 -U postgres -d queue_db
```

Пароль:

```
postgres
```

или пароль, указанный в `docker-compose.yml`.

### Полезные команды psql

```sql
\dt
```

Показать таблицы.

```sql
SELECT * FROM users;
```

Посмотреть всех пользователей.

---

## 🔹 Советы

- Перед первым запуском убедись, что **порт 5433 свободен**.
- Если база не создаётся — проверь **переменные окружения в `docker-compose.yml`**.
- Для тестирования API удобно использовать **Postman**.
