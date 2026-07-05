package org.example.coupleservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateCoupleRequest {
    @NotNull
    private UUID receiverId;
}
