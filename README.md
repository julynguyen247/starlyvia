# Starlyvia

Starlyvia is a Java 25 Spring Boot microservice project. It currently contains an authentication service backed by PostgreSQL and an API gateway service intended to front the backend services.

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
`-- auth-service/
    |-- pom.xml
    `-- src/
```

## Services

| Service | Description | Default Port |
| --- | --- | --- |
| `api-gateway` | Spring Cloud Gateway application | `8080` |
| `auth-service` | Authentication API with registration, login, and token validation | `8081` |
| `postgres` | PostgreSQL database for `auth-service` | `5433` on host |

The gateway routes `/api/v1/auth/**` traffic to `auth-service` and validates JWTs for protected auth routes.

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

This repository includes a `docker-compose.yml` for PostgreSQL, `auth-service`, and `api-gateway`.

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
  postgres:
    image: postgres:16-alpine
    container_name: starlyvia-postgres
    environment:
      POSTGRES_DB: auth_db
      POSTGRES_USER: starlyvia
      POSTGRES_PASSWORD: starlyvia
    ports:
      - "5433:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
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

The auth service test profile uses an in-memory H2 database from `auth-service/src/test/resources/application-test.yaml`.

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

The packaged applications are generated under each module's `target/` directory.
