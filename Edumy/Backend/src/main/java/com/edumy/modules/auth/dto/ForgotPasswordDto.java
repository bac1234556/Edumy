package com.edumy.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDto {
    @NotBlank
    @Email(message = "Invalid email address format.")
    private String email = "";
}
