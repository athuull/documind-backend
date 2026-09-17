package com.athul.documind.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequestDTO {

    private Long id;
    private String name;
    private String email;
    private String password;
    @NotNull(message = "Role ID is required")
    private Long roleId;
}