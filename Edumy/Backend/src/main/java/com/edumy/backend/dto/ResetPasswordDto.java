package com.edumy.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotBlank
    @Email(message = "Invalid email address format.")
    private String email = "";

    @NotBlank
    private String token = "";

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    private String newPassword = "";
}
