package com.athul.documind.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDTO {

    private Long id;
    private String name;
    private String email;
    private Long roleId;
    private Boolean isActive;
    private Long version;
}