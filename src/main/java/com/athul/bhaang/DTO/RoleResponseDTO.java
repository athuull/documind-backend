package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.RoleType;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class RoleResponseDTO {
    private Long id;
    private RoleType name;
    private Long version;
}
