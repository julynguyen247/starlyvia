package org.example.friendservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateFriendRequest {
    @NotNull
    private UUID receiverId;
}
