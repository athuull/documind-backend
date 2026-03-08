package com.athul.bhaang.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDTO {

    private String answer;
    private int totalTokens;
    private int promptTokens;
    private int completionTokens;
}
