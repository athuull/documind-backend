package com.athul.documind.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientResponseDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String createdByUserName;
    private String assignedToUserName;
    private Long version;
}