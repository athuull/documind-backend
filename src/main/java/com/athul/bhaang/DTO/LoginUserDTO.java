package com.athul.bhaang.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class LoginUserDTO {

    private String email;

    private String password;

}

