# gRPC and Kafka Flow

```mermaid
flowchart LR
    Client[Client / API Gateway]
    Auth[auth-service]
    Couple[couple-service]
    AuthDb[(auth_db)]
    CoupleDb[(couple_db)]
    Kafka[(Kafka)]
    Consumers[Future consumers<br/>notification-service<br/>dateplan-service]

    Client -- REST register / login --> Auth
    Auth -- write user --> AuthDb
    Auth -- Kafka event<br/>user.registered --> Kafka

    Client -- REST couple request --> Couple
    Couple -- gRPC UserExists(receiverId) --> Auth
    Auth -- exists / not exists --> Couple
    Couple -- write request / couple --> CoupleDb
    Couple -- Kafka events<br/>couple.requested<br/>couple.accepted<br/>couple.rejected<br/>couple.removed --> Kafka

    Kafka -- async consume --> Consumers
```

## Rule of Thumb

- gRPC is for synchronous service-to-service checks that need an immediate answer.
- Kafka is for asynchronous domain events that other services can react to later.
