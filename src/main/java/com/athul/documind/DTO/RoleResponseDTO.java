package com.athul.documind.DTO;

import com.athul.documind.Enum.RoleType;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class RoleResponseDTO {
    private Long id;
    private RoleType name;
    private String description;
    private Long version;
}
