# Starlyvia

Starlyvia is a Java 25 Spring Boot microservice project. It contains authentication and friendship services backed by PostgreSQL, plus an API gateway that fronts the backend services.

## Tech Stack

- Java 25
- Spring Boot 4.1.0
- Spring Security
- Spring Cloud Gateway
- Spring Data JPA
- PostgreSQL
- Docker Compose
- Maven Wrapper

## Project Structure

```text
.
|-- api-gateway/
|   |-- pom.xml
|   `-- src/
|-- auth-service/
|   |-- pom.xml
|   `-- src/
`-- friend-service/
    |-- pom.xml
    `-- src/
```

## Services

| Service | Description | Default Port |
| --- | --- | --- |
| `api-gateway` | Spring Cloud Gateway application | `8080` |
| `auth-service` | Authentication API with registration, login, and token validation | `8081` |
| `friend-service` | Friend request and friendship API | `8082` |
| `auth-postgres` | PostgreSQL database for `auth-service` | `5433` on host |
| `friend-postgres` | PostgreSQL database for `friend-service` | `5434` on host |

The gateway routes `/api/v1/auth/**` traffic to `auth-service`, routes `/api/v1/friends/**` traffic to `friend-service`, and validates JWTs for protected routes. `auth-service` owns the `users` table; `friend-service` stores only user UUIDs from JWT claims in `friend_requests` and `friendships`.

## Prerequisites

- JDK 25
- Docker and Docker Compose
- Bash-compatible shell

Each service includes its own Maven Wrapper, so a system Maven installation is optional.

## Database

The auth service is configured to connect to:

```text
jdbc:postgresql://localhost:5433/auth_db
username: starlyvia
password: starlyvia
```

The friend service is configured to connect to:

```text
jdbc:postgresql://localhost:5434/friend_db
username: starlyvia
password: starlyvia
```

This repository includes a `docker-compose.yml` for separate auth and friend PostgreSQL containers, `auth-service`, `friend-service`, and `api-gateway`.

Start the full stack:

```bash
docker compose up --build
```

Start it in the background:

```bash
docker compose up --build -d
```

Stop the stack:

```bash
docker compose down
```

Remove the PostgreSQL volume as well:

```bash
docker compose down -v
```

The Compose file uses this PostgreSQL service:

```yaml
services:
  auth-postgres:
    image: postgres:16-alpine
    container_name: starlyvia-auth-postgres
    environment:
      POSTGRES_DB: auth_db
      POSTGRES_USER: starlyvia
      POSTGRES_PASSWORD: starlyvia
    ports:
      - "5433:5432"

  friend-postgres:
    image: postgres:16-alpine
    container_name: starlyvia-friend-postgres
    environment:
      POSTGRES_DB: friend_db
      POSTGRES_USER: starlyvia
      POSTGRES_PASSWORD: starlyvia
    ports:
      - "5434:5432"

volumes:
  auth-postgres-data:
  friend-postgres-data:
```

## Running Locally

Start PostgreSQL first, then run the services in separate terminals.

Run the auth service:

```bash
cd auth-service
./mvnw spring-boot:run
```

Run the API gateway:

```bash
cd api-gateway
./mvnw spring-boot:run
```

Run the friend service:

```bash
cd friend-service
./mvnw spring-boot:run
```

## Auth API

Base URL:

```text
http://localhost:8080/api/v1/auth
```

Register a user:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "username": "starlyvia_user"
  }'
```

Log in:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
}'
```

## Friend API

Base URL:

```text
http://localhost:8080/api/v1/friends
```

Send a friend request:

```bash
curl -X POST http://localhost:8080/api/v1/friends/requests \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"receiverId":"<receiver-user-id>"}'
```

Accept a friend request:

```bash
curl -X POST http://localhost:8080/api/v1/friends/requests/<request-id>/accept \
  -H "Authorization: Bearer <token>"
```

List current user's friendships:

```bash
curl http://localhost:8080/api/v1/friends \
  -H "Authorization: Bearer <token>"
```

## Swagger UI

After starting the stack, open:

```text
http://localhost:8080/swagger-ui.html
```

The Swagger UI is served through the API gateway and can be used to test the `/api/v1/auth/register` and `/api/v1/auth/login` endpoints.

## Configuration

Auth service configuration is in:

```text
auth-service/src/main/resources/application.yaml
```

Friend service configuration is in:

```text
friend-service/src/main/resources/application.yaml
```

Important properties:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/auth_db
    username: starlyvia
    password: starlyvia

jwt:
  secret: "starlyvia-super-secret-key-starlyvia-super-secret-key"
  expiration: 36000000
```

For production, move secrets and database credentials to environment variables or a secrets manager.

## Testing

Run tests for the auth service:

```bash
cd auth-service
./mvnw test
```

Run tests for the API gateway:

```bash
cd api-gateway
./mvnw test
```

Run tests for the friend service:

```bash
cd friend-service
./mvnw test
```

The auth and friend service test profiles use in-memory H2 databases from their `src/test/resources/application-test.yaml` files.

## Build

Build each service:

```bash
cd auth-service
./mvnw clean package
```

```bash
cd api-gateway
./mvnw clean package
```

```bash
cd friend-service
./mvnw clean package
```

The packaged applications are generated under each module's `target/` directory.
