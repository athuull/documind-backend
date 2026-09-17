package com.athul.documind.DTO;

import com.athul.documind.Enum.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCreateRequestDTO {

    private RoleType name;   // NOT String
    private String description;
}