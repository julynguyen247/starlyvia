# gRPC and Kafka Flow

```mermaid
flowchart LR
    Client[Client / API Gateway]
    Auth[auth-service]
    Group[group-service]
    AuthDb[(auth_db)]
    GroupDb[(group_db)]
    Kafka[(Kafka)]
    Consumers[Future consumers<br/>notification-service<br/>plan-service]

    Client -- REST register / login --> Auth
    Auth -- write user --> AuthDb
    Auth -- Kafka event<br/>user.registered --> Kafka

    Client -- REST create group / invite member --> Group
    Group -- gRPC UserExists(inviteeId) --> Auth
    Auth -- exists / not exists --> Group
    Group -- write group / member / invitation --> GroupDb
    Group -- Kafka events<br/>group.created<br/>group.invitation.created<br/>group.member.added<br/>group.member.removed --> Kafka

    Kafka -- async consume --> Consumers
```

## Rule of Thumb

- gRPC is for synchronous service-to-service checks that need an immediate answer.
- Kafka is for asynchronous domain events that other services can react to later.
