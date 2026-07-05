package com.example.myself_resume_analyzer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO  implements Serializable {
    private String token;
    private Long userId;
    private String username;
}
