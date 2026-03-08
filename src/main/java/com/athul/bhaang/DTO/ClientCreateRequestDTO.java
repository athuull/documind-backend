package com.athul.bhaang.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientCreateRequestDTO {
    private String name;
    private String phone;
    private String email;
    private String address;
}