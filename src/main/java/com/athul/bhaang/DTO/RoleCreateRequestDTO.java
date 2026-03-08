package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCreateRequestDTO {

    private RoleType name;   // NOT String
    private String description;
}