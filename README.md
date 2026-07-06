# Starlyvia

Starlyvia is a Java 25 Spring Boot microservice project. It contains authentication and group services backed by PostgreSQL, plus an API gateway that fronts the backend services.

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
|-- group-service/
|   |-- pom.xml
|   `-- src/
`-- plan-service/
    |-- pom.xml
    `-- src/
```

## Services

| Service | Description | Default Port |
| --- | --- | --- |
| `api-gateway` | Spring Cloud Gateway application | `8080` |
| `auth-service` | Authentication API with registration, login, and token validation | `8081` |
| `group-service` | Group, membership, and invitation API | `8082` |
| `plan-service` | Plan and stop scheduling API | `8083` |
| `auth-postgres` | PostgreSQL database for `auth-service` | `5433` on host |
| `group-postgres` | PostgreSQL database for `group-service` | `5434` on host |
| `plan-postgres` | PostgreSQL database for `plan-service` | `5435` on host |

The gateway routes `/api/v1/auth/**` traffic to `auth-service`, `/api/v1/groups/**` traffic to `group-service`, and plan traffic to `plan-service`. Protected routes are validated with JWT. `auth-service` owns the `users` table; `group-service` stores group membership; `plan-service` stores plans and stops.

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

The group service is configured to connect to:

```text
jdbc:postgresql://localhost:5434/group_db
username: starlyvia
password: starlyvia
```

The plan service is configured to connect to:

```text
jdbc:postgresql://localhost:5435/plan_db
username: starlyvia
password: starlyvia
```

This repository includes a `docker-compose.yml` for separate auth, group, and plan PostgreSQL containers, `auth-service`, `group-service`, `plan-service`, and `api-gateway`.

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

  group-postgres:
    image: postgres:16-alpine
    container_name: starlyvia-group-postgres
    environment:
      POSTGRES_DB: group_db
      POSTGRES_USER: starlyvia
      POSTGRES_PASSWORD: starlyvia
    ports:
      - "5434:5432"

volumes:
  auth-postgres-data:
  group-postgres-data:
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

Run the group service:

```bash
cd group-service
./mvnw spring-boot:run
```

Run the plan service:

```bash
cd plan-service
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

## Group API

Base URL:

```text
http://localhost:8080/api/v1/groups
```

Create a group:

```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Weekend plan",
    "type": "FRIENDS"
  }'
```

Invite a user to a group:

```bash
curl -X POST http://localhost:8080/api/v1/groups/<group-id>/invitations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"inviteeId":"<invitee-user-id>"}'
```

Accept a group invitation:

```bash
curl -X POST http://localhost:8080/api/v1/groups/invitations/<invitation-id>/accept \
  -H "Authorization: Bearer <token>"
```

List current user's groups:

```bash
curl http://localhost:8080/api/v1/groups \
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

Group service configuration is in:

```text
group-service/src/main/resources/application.yaml
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

Run tests for the group service:

```bash
cd group-service
./mvnw test
```

Run tests for the plan service:

```bash
cd plan-service
./mvnw test
```

The auth, group, and plan service test profiles use in-memory H2 databases from their `src/test/resources/application-test.yaml` files.

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
cd group-service
./mvnw clean package
```

```bash
cd plan-service
./mvnw clean package
```

The packaged applications are generated under each module's `target/` directory.
