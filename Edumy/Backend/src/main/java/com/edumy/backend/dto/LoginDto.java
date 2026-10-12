package com.edumy.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank
    @Email(message = "Invalid email address format.")
    private String email = "";

    @NotBlank
    private String password = "";
}
