package org.example.groupservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.groupservice.entity.GroupType;

@Getter
@Setter
public class CreateGroupRequest {
    @NotBlank
    private String name;

    private String description;

    private GroupType type = GroupType.CUSTOM;
}
