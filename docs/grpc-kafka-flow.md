# gRPC and Kafka Flow

```mermaid
flowchart LR
    Client[Client / API Gateway]
    Auth[auth-service]
    Group[group-service]
    AuthDb[(auth_db)]
    GroupDb[(group_db)]
    Kafka[(Kafka)]
    Consumers[notification-service<br/>Future consumers<br/>plan-service]

    Client -- REST register / login --> Auth
    Auth -- write user --> AuthDb
    Auth -- Kafka domain topic<br/>auth.events<br/>eventType=user.registered --> Kafka

    Client -- REST create group / invite member --> Group
    Group -- gRPC UserExists(inviteeId) --> Auth
    Auth -- exists / not exists --> Group
    Group -- write group / member / invitation --> GroupDb
    Group -- Kafka domain topic<br/>group.events<br/>eventType=group.* --> Kafka

    Kafka -- async consume --> Consumers
```

## Rule of Thumb

- gRPC is for synchronous service-to-service checks that need an immediate answer.
- Kafka is for asynchronous domain events that other services can react to later.
