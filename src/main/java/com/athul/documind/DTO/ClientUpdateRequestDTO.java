package com.athul.documind.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientUpdateRequestDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Long assignedToUserId; // To reassign the client
    private Long version;          // Critical for Optimistic Locking
}