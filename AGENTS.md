# Commands

## Start the postgresql container

```bash
cd queue-project && docker compose up -d && cd -`
```

## Stop and delete the postgresql container

```bash
cd queue-project && docker compose down && cd -
```

## Build Spring Boot project

```bash
cd queue-project && mvn clean install && cd -
```

## Run Spring Boot app

```bash
cd queue-project && mvn spring-boot:run && cd -
```

## Manual testing registration

```bash
curl -X POST https://127.0.0.1:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "123456",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

## Manual testing login

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "123456"
  }'
```

# Boundaries

## Always do

- Run test suite before submitting a PR
- Fix linting errors surfaced by ruff
- Add docstrings to new public functions
- Stage only explicitly requested files

## Ask first

- Any change to database schema files (queue-project/queue-project/src/main/resources/db/migration/)
- Updating external API credentials or environment variables
- Adding new dependencies to queue-project/queue-project/pom.xml

## Never do

- Push directly to main or staging
- Run migrations against the database
- Stage all files (never git add -A)
