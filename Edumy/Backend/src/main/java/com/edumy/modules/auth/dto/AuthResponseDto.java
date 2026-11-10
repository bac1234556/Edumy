package com.edumy.modules.auth.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String token = "";
    private String fullName = "";
    private String email = "";
    private String role = "";
    private String message = "";
}
