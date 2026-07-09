# Starlyvia

Starlyvia is a Java 25 Spring Boot microservice project. It contains authentication, group, plan, place, and notification services, plus an API gateway that fronts the backend services.

## Tech Stack

- Java 25
- Spring Boot 4.1.0
- Spring Security
- Spring Cloud Gateway
- Spring Data JPA
- PostgreSQL
- Kafka
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
|-- plan-service/
|   |-- pom.xml
|   `-- src/
|-- place-service/
|   |-- pom.xml
|   `-- src/
`-- notification-service/
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
| `place-service` | Place autocomplete, details, and nearby search API | `8084` |
| `notification-service` | Notification API and Kafka event consumer | `8085` |
| `kafka` | Domain event broker | `9094` on host |
| `auth-postgres` | PostgreSQL database for `auth-service` | `5433` on host |
| `group-postgres` | PostgreSQL database for `group-service` | `5434` on host |
| `plan-postgres` | PostgreSQL database for `plan-service` | `5435` on host |
| `notification-postgres` | PostgreSQL database for `notification-service` | `5436` on host |

The gateway routes `/api/v1/auth/**` traffic to `auth-service`, `/api/v1/groups/**` traffic to `group-service`, plan traffic to `plan-service`, `/api/v1/places/**` traffic to `place-service`, and `/api/v1/notifications/**` traffic to `notification-service`. Protected routes are validated with JWT. `auth-service` owns the `users` table; `group-service` stores group membership; `plan-service` stores plans and stops; `place-service` proxies external map/place providers; `notification-service` stores per-user notifications and consumes domain events from Kafka.

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

The notification service is configured to connect to:

```text
jdbc:postgresql://localhost:5436/notification_db
username: starlyvia
password: starlyvia
```

This repository includes a `docker-compose.yml` for Kafka, separate auth, group, plan, and notification PostgreSQL containers, `auth-service`, `group-service`, `plan-service`, `place-service`, `notification-service`, and `api-gateway`.

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

The Compose file uses these PostgreSQL services:

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

  plan-postgres:
    image: postgres:16-alpine
    container_name: starlyvia-plan-postgres
    environment:
      POSTGRES_DB: plan_db
      POSTGRES_USER: starlyvia
      POSTGRES_PASSWORD: starlyvia
    ports:
      - "5435:5432"

  notification-postgres:
    image: postgres:16-alpine
    container_name: starlyvia-notification-postgres
    environment:
      POSTGRES_DB: notification_db
      POSTGRES_USER: starlyvia
      POSTGRES_PASSWORD: starlyvia
    ports:
      - "5436:5432"

volumes:
  auth-postgres-data:
  group-postgres-data:
  plan-postgres-data:
  notification-postgres-data:
```

## Running Locally

Start PostgreSQL and Kafka first, then run the services in separate terminals.

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

Run the place service:

```bash
cd place-service
GOOGLE_PLACES_API_KEY=<google-places-api-key> ./mvnw spring-boot:run
```

Run the notification service:

```bash
cd notification-service
./mvnw spring-boot:run
```

## Kafka Topics

Domain events are grouped by service domain:

```text
auth.events
group.events
plan.events
```

The event action is carried in the JSON payload as `eventType`, such as `user.registered`, `group.invitation.created`, `group.member.added`, `group.member.removed`, `plan.created`, `plan.updated`, and `plan.deleted`.

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

## Place API

Base URL:

```text
http://localhost:8080/api/v1/places
```

Autocomplete places:

```bash
curl "http://localhost:8080/api/v1/places/autocomplete?query=cafe&lat=10.7769&lng=106.7009&sessionToken=<uuid>" \
  -H "Authorization: Bearer <token>"
```

Get place details:

```bash
curl "http://localhost:8080/api/v1/places/details?provider=GOOGLE&providerPlaceId=<google-place-id>" \
  -H "Authorization: Bearer <token>"
```

Search nearby places:

```bash
curl "http://localhost:8080/api/v1/places/nearby?lat=10.7769&lng=106.7009&type=restaurant" \
  -H "Authorization: Bearer <token>"
```

## Notification API

Base URL:

```text
http://localhost:8080/api/v1/notifications
```

List current user's notifications:

```bash
curl "http://localhost:8080/api/v1/notifications?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

Get unread count:

```bash
curl http://localhost:8080/api/v1/notifications/unread-count \
  -H "Authorization: Bearer <token>"
```

Mark a notification as read:

```bash
curl -X PATCH http://localhost:8080/api/v1/notifications/<notification-id>/read \
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

Place service configuration is in:

```text
place-service/src/main/resources/application.yaml
```

Notification service configuration is in:

```text
notification-service/src/main/resources/application.yaml
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

`place-service` uses `GOOGLE_PLACES_API_KEY` to call Google Places. Without that value, the service starts, but place lookup endpoints return `503`.

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

Run tests for the place service:

```bash
cd place-service
./mvnw test
```

Run tests for the notification service:

```bash
cd notification-service
./mvnw test
```

The auth, group, plan, and notification service test profiles use in-memory H2 databases from their `src/test/resources/application-test.yaml` files.

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

```bash
cd place-service
./mvnw clean package
```

```bash
cd notification-service
./mvnw clean package
```

The packaged applications are generated under each module's `target/` directory.
