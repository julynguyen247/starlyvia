package org.example.groupservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InviteGroupMemberRequest {
    @NotNull
    private UUID inviteeId;
}
