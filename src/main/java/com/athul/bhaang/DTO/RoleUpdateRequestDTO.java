// RoleUpdateRequestDTO.java
package com.athul.bhaang.DTO;

import com.athul.bhaang.Enum.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleUpdateRequestDTO {
    private Long id;
    private RoleType name;
    private String description;
    private Long version; // <--- Needed for locking check
}